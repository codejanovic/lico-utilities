package org.lico.utilities.webcam.streamer.ffmpeg;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamException;
import org.bridj.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class FFmpegCliDriver implements WebcamDriver, WebcamDiscoverySupport {

    private static final Logger LOG = LoggerFactory.getLogger(FFmpegCliDriver.class);

    private static final VideoDeviceFilenameFilter VFFILTER = new VideoDeviceFilenameFilter();

    private String path = "";

    public static String getCaptureDriver() {
        if (Platform.isLinux()) {
            return "video4linux2";
        } else if (Platform.isWindows()) {
            return "dshow";
        } else if (Platform.isMacOSX()) {
            return "avfoundation";
        }

        // Platform not supported
        return null;
    }

    public static String getCommand(final String path) {
        return path + "ffmpeg.exe";
    }

    @Override
    public List<WebcamDevice> getDevices() {
        final List<WebcamDevice> devices;

        if (Platform.isWindows()) {
            devices = getWindowsDevices();
        } else {
            devices = getUnixDevices();
        }

        return devices;
    }

    private List<WebcamDevice> getUnixDevices() {
        final File[] vfiles = VFFILTER.getVideoFiles();

        final List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

        String line = null;
        BufferedReader br = null;

        for (final File vfile : vfiles) {

            final String[] cmd = new String[]{
                    getCommand(),
                    "-f", FFmpegCliDriver.getCaptureDriver(),
                    "-hide_banner", "",
                    "-list_formats", "all",
                    "-i", vfile.getAbsolutePath(),
            };

            final Process process = startProcess(cmd);

            final InputStream is = process.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            final String STARTER = "[" + FFmpegCliDriver.getCaptureDriver();
            final String MARKER = "] Raw";

            try {
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(STARTER) && line.contains(MARKER)) {
                        LOG.debug("Command stdout line: {}", line);
                        final String resolutions = line.split(" : ")[3].trim();
                        devices.add(new FFmpegCliDevice(path, vfile, resolutions));
                        break;
                    }
                }

            } catch (final IOException e) {
                throw new WebcamException(e);
            } finally {
                try {
                    is.close();
                } catch (final IOException e) {
                    throw new WebcamException(e);
                }
                process.destroy();
                try {
                    process.waitFor();
                } catch (final InterruptedException e) {
                    throw new WebcamException(e);
                }
            }
        }
        return devices;
    }

    private List<WebcamDevice> getWindowsDevices() {

        final List<String> devicesNames = getWindowsDevicesNames();

        final List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

        for (final String deviceName : devicesNames) {
            devices.add(buildWindowsDevice(deviceName));
        }

        return devices;
    }

    private WebcamDevice buildWindowsDevice(final String deviceName) {
        final String deviceInput = "\"video=" + deviceName + "\"";

        final String[] cmd = new String[]{getCommand(), "-list_options", "true", "-f", FFmpegCliDriver.getCaptureDriver(), "-hide_banner", "", "-i", deviceInput};

        final Process listDevicesProcess = startProcess(cmd);

        final String STARTER = "[" + FFmpegCliDriver.getCaptureDriver();
        final String MARKER = "max s=";

        final Set<String> resolutions = new LinkedHashSet<>();

        InputStream is = null;
        final BufferedReader br;
        String line;
        try {
            is = listDevicesProcess.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                if (line.startsWith(STARTER) && line.contains(MARKER)) {
                    final int begin = line.indexOf(MARKER) + MARKER.length();
                    final String resolution = line.substring(begin, line.indexOf(" ", begin));
                    resolutions.add(resolution);
                }
            }

        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        final StringBuilder vinfo = new StringBuilder();
        for (final String resolution : resolutions) {
            vinfo.append(resolution).append(" ");
        }

        return new FFmpegCliDevice(path, deviceName, vinfo.toString().trim());
    }

    private List<String> getWindowsDevicesNames() {
        final String[] cmd = new String[]{getCommand(), "-list_devices", "true", "-f", FFmpegCliDriver.getCaptureDriver(), "-hide_banner", "", "-i", "dummy"};
        final Process listDevicesProcess = startProcess(cmd);

        final List<String> devicesNames = new ArrayList<>();

        final String STARTER = "[" + FFmpegCliDriver.getCaptureDriver();
        final String NAME_MARKER = "]  \"";
        final String VIDEO_MARKER = "] DirectShow video";
        final String AUDIO_MARKER = "] DirectShow audio";

        boolean startDevices = false;

        InputStream is = null;
        final BufferedReader br;
        String line;
        try {
            is = listDevicesProcess.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                if (line.startsWith(STARTER) && line.contains(VIDEO_MARKER)) {
                    startDevices = true;
                    continue;
                }
                if (startDevices) {
                    if (line.startsWith(STARTER) && line.contains(NAME_MARKER)) {
                        String deviceName = line.substring(line.indexOf(NAME_MARKER) + NAME_MARKER.length());
                        // Remove final double quotes
                        deviceName = deviceName.substring(0, deviceName.length() - 1);
                        devicesNames.add(deviceName);
                        continue;
                    }
                    if (line.startsWith(STARTER) && line.contains(AUDIO_MARKER)) {
                        break;
                    }
                }
            }

        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        return devicesNames;
    }

    private Process startProcess(final String[] cmd) {
        Process process = null;

        final OutputStream os;
        if (LOG.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            for (final String c : cmd) {
                sb.append(c).append(' ');
            }
            LOG.debug("Executing command: {}", sb.toString());
        }

        try {
            final ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            process = builder.start();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        os = process.getOutputStream();

        try {
            os.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return process;
    }

    public FFmpegCliDriver withPath(final String path) {
        this.path = path;
        return this;
    }

    private String getCommand() {
        return getCommand(path);
    }

    @Override
    public boolean isThreadSafe() {
        return false;
    }

    @Override
    public long getScanInterval() {
        return 3000;
    }

    @Override
    public boolean isScanPossible() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}