package org.lico.utilities.webcam.streamer.webcam;


import org.lico.utilities.webcamcapture.drivers.capturemanager.CaptureManagerVideoDevice;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamUtils;
import org.lico.utilities.webcam.streamer.inject.Eventbus;
import org.lico.utilities.webcam.streamer.settings.ApplicationSettings;
import org.greenrobot.eventbus.Subscribe;
import org.jusecase.inject.Component;

import javax.inject.Inject;
import java.util.Objects;

public interface Webcamera {

    String identifier();

    String name();

    String shortName();

    byte[] takeImage();

    boolean isOpen();

    WebcameraImageStream stream();

    String urlToStream();

    String urlToSSnapshot();

    void open();

    void close();

    final class OpenEvent {
        public final Webcamera webcamera;

        public OpenEvent(final Webcamera webcamera) {
            this.webcamera = webcamera;
        }
    }

    final class OpenedEvent {
        public final Webcamera webcamera;

        public OpenedEvent(final Webcamera webcamera) {
            this.webcamera = webcamera;
        }
    }


    final class CloseEvent {
        public final Webcamera webcamera;

        public CloseEvent(final Webcamera webcamera) {
            this.webcamera = webcamera;
        }
    }

    final class ClosedEvent {
        public final Webcamera webcamera;

        public ClosedEvent(final Webcamera webcamera) {
            this.webcamera = webcamera;
        }
    }

    final class SubscribeStreamEvent {
        private final Webcamera webcamera;
        private final WebcamStreamSubscriber subscriber;

        public SubscribeStreamEvent(final Webcamera webcamera, final WebcamStreamSubscriber subscriber) {
            this.webcamera = webcamera;
            this.subscriber = subscriber;
        }
    }

    final class UnsubscribeStreamEvent {
        private final Webcamera webcamera;
        private final WebcamStreamSubscriber subscriber;

        public UnsubscribeStreamEvent(final Webcamera webcamera, final WebcamStreamSubscriber subscriber) {
            this.webcamera = webcamera;
            this.subscriber = subscriber;
        }
    }

    final class UnsubscribedStreamEvent {
        public final Webcamera webcamera;
        public final WebcamStreamSubscriber subscriber;

        public UnsubscribedStreamEvent(final Webcamera webcamera, final WebcamStreamSubscriber subscriber) {
            this.webcamera = webcamera;
            this.subscriber = subscriber;
        }
    }


    @Component
    final class Native implements Webcamera {

        private final Webcam _webcam;
        private final WebcameraImageStream _stream;
        private final String _name;
        private final String              _identifier;
        @Inject
        private       Eventbus            _eventbus;
        @Inject
        private       ApplicationSettings _settings;

        public Native(final Webcam webcam) {
            _eventbus.register(this);
            _webcam = webcam;
            _name = _webcam.getName();
            _identifier = _webcam.getDevice() instanceof CaptureManagerVideoDevice ? ((CaptureManagerVideoDevice) _webcam.getDevice()).getId() : _webcam.getName();
            _stream = new WebcameraImageStream.Loop(this, FramesPerSecond.FPS_10);
        }

        @Subscribe
        public void onStreamSubscribe(final SubscribeStreamEvent event) {
            if (!event.webcamera.equals(this)) {
                return;
            }
            _stream.subscribe(event.subscriber);
        }

        @Subscribe
        public void onStreamUnsubscribe(final UnsubscribeStreamEvent event) {
            if (!event.webcamera.equals(this)) {
                return;
            }
            _stream.unsubscribe(event.subscriber);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Native aNative = (Native) o;
            return Objects.equals(_identifier, aNative._identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(_identifier);
        }

        @Override
        public String identifier() {
            return _identifier;
        }

        @Override
        public String name() {
            return String.format("%s (%s)", _name, shortName());
        }

        @Override
        public String shortName() {
            return _settings.findOrCreate(this).getShortName();
        }

        @Override
        public byte[] takeImage() {
            return WebcamUtils.getImageBytes(_webcam, "jpg");
        }

        @Override
        public boolean isOpen() {
            return _webcam.isOpen();
        }

        @Override
        public WebcameraImageStream stream() {
            return _stream;
        }

        @Override
        public String urlToStream() {
            return String.format("http://localhost:%s/%s/mjpeg", _settings.getServer().getPort(), shortName());
        }

        @Override
        public String urlToSSnapshot() {
            return String.format("http://localhost:%s/%s/snapshot", _settings.getServer().getPort(), shortName());
        }


        public void open() {
            _webcam.open();
            _stream.open();
            _eventbus.send(new OpenedEvent(this));
        }


        public void close() {
            _stream.close();
            _webcam.close();
            _eventbus.send(new ClosedEvent(this));
        }

        @Subscribe
        public void onWebcameraCloseEvent(final Webcamera.CloseEvent event) {
            if (!event.webcamera.equals(this)) {
                return;
            }
            close();
        }

        @Subscribe
        public void onWebcameraOpenEvent(final Webcamera.OpenEvent event) {
            if (!event.webcamera.equals(this)) {
                return;
            }
            open();
        }
    }
}
