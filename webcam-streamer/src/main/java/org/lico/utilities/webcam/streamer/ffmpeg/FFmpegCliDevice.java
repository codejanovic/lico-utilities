package org.lico.utilities.webcam.streamer.ffmpeg;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import org.bridj.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class FFmpegCliDevice implements WebcamDevice, WebcamDevice.BufferAccess {

    private static final Logger LOG = LoggerFactory.getLogger(FFmpegCliDevice.class);
    private final AtomicBoolean open = new AtomicBoolean(false);
    private final AtomicBoolean disposed = new AtomicBoolean(false);
    private volatile Process process = null;
    private String path = "";
    private String name = null;
    private Dimension[] resolutions = null;
    private Dimension resolution = null;

    protected FFmpegCliDevice(final String path, final File vfile, final String resolutions) {
        this(path, vfile.getAbsolutePath(), resolutions);
    }

    protected FFmpegCliDevice(final String path, final String name, final String resolutions) {
        this.path = path;
        this.name = name;
        this.resolutions = readResolutions(resolutions);
    }

    private static boolean isAlive(final Process p) {
        try {
            p.exitValue();
            return false;
        } catch (final IllegalThreadStateException e) {
            return true;
        }
    }

    public void startProcess() throws IOException {
        final ProcessBuilder builder = new ProcessBuilder(buildCommand());
        builder.redirectErrorStream(true); // so we can ignore the error stream

        process = builder.start();
    }

    private byte[] readNextFrame() throws IOException {
        final InputStream out = process.getInputStream();

        final int SIZE = arraySize();
        final int CHUNK_SIZE = SIZE / 20;

        int cursor = 0;
        final byte[] buffer = new byte[SIZE];

        while (isAlive(process)) {
            final int no = out.available();
            if (no >= CHUNK_SIZE) {

                // If buffer is not full yet
                if (cursor < SIZE) {
                    out.read(buffer, cursor, CHUNK_SIZE);
                    cursor += CHUNK_SIZE;
                } else {
                    break;
                }
            }
        }

        return buffer;
    }

    /**
     * Based on answer: https://stackoverflow.com/a/12062505/7030976
     *
     * @param bgr - byte array in bgr format
     * @return new image
     */
    private BufferedImage buildImage(final byte[] bgr) {
        final BufferedImage image = new BufferedImage(resolution.width, resolution.height, BufferedImage.TYPE_3BYTE_BGR);
        final byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(bgr, 0, imageData, 0, bgr.length);

        return image;
    }

    private Dimension[] readResolutions(final String res) {
        final List<Dimension> resolutions = new ArrayList<Dimension>();
        final String[] parts = res.split(" ");

        for (final String part : parts) {
            final String[] xy = part.split("x");
            resolutions.add(new Dimension(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
        }

        return resolutions.toArray(new Dimension[resolutions.size()]);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Dimension[] getResolutions() {
        return resolutions;
    }

    @Override
    public Dimension getResolution() {
        if (resolution == null) {
            resolution = getResolutions()[0];
        }
        return resolution;
    }

    @Override
    public void setResolution(final Dimension resolution) {
        this.resolution = resolution;
    }

    private String getResolutionString() {
        final Dimension d = getResolution();
        return String.format("%dx%d", d.width, d.height);
    }

    @Override
    public void open() {
        if (!open.compareAndSet(false, true)) {
            return;
        }

        try {
            startProcess();
        } catch (final IOException e) {
            throw new WebcamException(e);
        }
    }

    @Override
    public void close() {
        if (!open.compareAndSet(true, false)) {
            return;
        }

        process.destroy();

        try {
            process.waitFor();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose() {
        if (disposed.compareAndSet(false, true) && open.get()) {
            close();
        }
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    public String[] buildCommand() {
        final String captureDriver = FFmpegCliDriver.getCaptureDriver();

        String deviceInput = name;
        if (Platform.isWindows()) {
            deviceInput = "\"video=" + name + "\"";
        }

        return new String[]{
                FFmpegCliDriver.getCommand(path),
                "-loglevel", "panic", // suppress ffmpeg headers
                "-f", captureDriver, // camera format
                "-s", getResolutionString(), // frame dimension
                "-i", deviceInput, // input file
                "-vcodec", "rawvideo", // raw output
                "-f", "rawvideo", // raw output
                "-vf", "hflip", // flip image horizontally
                "-vsync", "vfr", // avoid frame duplication
                "-pix_fmt", "bgr24", // output format as bgr24
                "-", // output to stdout
        };
    }

    @Override
    public BufferedImage getImage() {
        if (!open.get()) {
            return null;
        }

        try {
            return buildImage(readNextFrame());
        } catch (final IOException e) {
            throw new WebcamException(e);
        }
    }

    @Override
    public ByteBuffer getImageBytes() {

        if (!open.get()) {
            return null;
        }

        final ByteBuffer buffer;
        try {
            buffer = ByteBuffer.allocate(arraySize());
            buffer.put(readNextFrame());
        } catch (final IOException e) {
            throw new WebcamException(e);
        }

        return buffer;
    }

    @Override
    public void getImageBytes(final ByteBuffer byteBuffer) {
        try {
            byteBuffer.put(readNextFrame());
        } catch (final IOException e) {
            throw new WebcamException(e);
        }
    }

    private int arraySize() {
        return resolution.width * resolution.height * 3;
    }
}