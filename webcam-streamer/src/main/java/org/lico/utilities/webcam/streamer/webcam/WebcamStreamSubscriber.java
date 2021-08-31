package org.lico.utilities.webcam.streamer.webcam;

import org.lico.utilities.webcam.streamer.inject.Eventbus;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.Subscribe;
import org.jusecase.inject.Component;
import org.lico.utilities.webcam.streamer.Bootstrap;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public interface WebcamStreamSubscriber {

    void consume(final byte[] bytes, final FramesPerSecond fps);

    boolean isSubscribed();

    @Component
    final class SnapshotProducer implements WebcamStreamSubscriber {
        private static final String _userDir = System.getProperty("user.dir");
        private final Path _snapshotFilePath;

        @Inject
        private Eventbus _eventbus;
        @Inject
        private Logger   _log;

        private final Webcamera _webcam;
        private final AtomicLong _fpsCounter = new AtomicLong(0);
        private final AtomicBoolean _shutdowned = new AtomicBoolean(false);
        private final OutputStream _output;

        public SnapshotProducer(final Webcamera webcam, final OutputStream output) {
            _snapshotFilePath = Paths.get(_userDir, "webcam-streamer", webcam.shortName() + ".jpg");
            _eventbus.register(this);
            _webcam = webcam;
            _output = output;
        }

        @Override
        public void consume(final byte[] bytes, final FramesPerSecond fps) {
            if (_shutdowned.get()) {
                return;
            }
            final long counter = _fpsCounter.incrementAndGet();
            final File snapshotFile;
            if (counter == 1 || counter == fps.getFrames()) {
                try {
                    snapshotFile = new File(_snapshotFilePath.toAbsolutePath().toString());
                    if (!snapshotFile.exists()) {
                        snapshotFile.getParentFile().mkdirs();
                        snapshotFile.createNewFile();
                    }
                    try (FileOutputStream fos = new FileOutputStream(snapshotFile)) {
                        fos.write(bytes);
                    }
                    //_output.write(("<img src=\"" + _snapshotFilePath.toAbsolutePath() + "\" style=\"width:100%;height:100%;\" alt=\"\" />").getBytes());
                    _output.write(("<!DOCTYPE html><html><body><img src=\"" + _snapshotFilePath.toAbsolutePath() + "\" style=\"width:100%;height:100%;\" alt=\"\" /></body></html>").getBytes());
                    _output.flush();
                    _output.close();

                    _eventbus.send(new Webcamera.UnsubscribeStreamEvent(_webcam, this));
                } catch (IOException e) {
                    _log.error("failed to write output.", e);
                } finally {
                    _shutdowned.set(true);
                    _eventbus.send(new Webcamera.UnsubscribeStreamEvent(_webcam, this));
                }
            }
        }

        @Override
        public boolean isSubscribed() {
            return !_shutdowned.get();
        }

        @Subscribe
        public void onUnsubscribed(final Webcamera.UnsubscribedStreamEvent event) {
            if (!event.subscriber.equals(this)) {
                return;
            }
            try {
                _output.close();
            } catch (IOException e) {
                _log.error("closing output on onUnsubscribed failed.", e);
            }
        }

    }

    @Component
    final class MjpegProducer implements WebcamStreamSubscriber {
        private final Webcamera _webcam;
        @Inject
        private Eventbus _eventbus;
        @Inject
        private Logger _log;
        private final OutputStream _output;
        private final AtomicBoolean _shutdowned = new AtomicBoolean(false);

        public MjpegProducer(final Webcamera webcam, final OutputStream output) {
            _eventbus.register(this);
            _webcam = webcam;
            _output = output;
        }

        @Override
        public boolean isSubscribed() {
            return !_shutdowned.get();
        }

        @Override
        public void consume(final byte[] bytes, final FramesPerSecond fps) {
            if (_shutdowned.get()) {
                return;
            }
            try {
                _output.write(("--" + Bootstrap.BOUNDARY + "\r\n" + "Content-Type:image/jpeg\r\n" + "Content-Length:" + bytes.length
                        + "\r\n\r\n").getBytes());
                _output.write(bytes);
                _output.write(("\r\n\r\n").getBytes());
                _output.flush();
            } catch (IOException e) {
                _shutdowned.set(true);
                _eventbus.send(new Webcamera.UnsubscribeStreamEvent(_webcam, this));
            }
        }

        @Subscribe
        public void onUnsubscribed(final Webcamera.UnsubscribedStreamEvent event) {
            if (!event.subscriber.equals(this)) {
                return;
            }
            try {
                _output.close();
            } catch (IOException e) {
                _log.error("closing output on onUnsubscribed failed.", e);
            }
        }
    }
}
