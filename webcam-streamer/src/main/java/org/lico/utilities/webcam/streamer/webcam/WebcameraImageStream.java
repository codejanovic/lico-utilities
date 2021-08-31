package org.lico.utilities.webcam.streamer.webcam;

import org.lico.utilities.webcam.streamer.inject.Eventbus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jusecase.inject.Component;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public interface WebcameraImageStream {

    void open();

    void close();

    void subscribe(WebcamStreamSubscriber subscriber);

    void unsubscribe(WebcamStreamSubscriber subscriber);

    @Component
    final class Loop implements WebcameraImageStream {
        private static final Logger _log = LogManager.getLogger(Loop.class);
        private final Queue<WebcamStreamSubscriber> _subscriber = new ConcurrentLinkedQueue<>();
        private final AtomicBoolean _stopped = new AtomicBoolean(false);
        private final ScheduledExecutorService _scheduler;
        private final FramesPerSecond _fps;
        private final Webcamera _webcam;
        @Inject
        private       Eventbus  _eventbus;

        public Loop(final Webcamera.Native webcam, final FramesPerSecond fps) {
            _webcam = webcam;
            _scheduler = Executors.newSingleThreadScheduledExecutor();
            _fps = fps;
        }

        private void process() {
            if (_stopped.get()) {
                _stopped.set(false);
                _log.info("{}: stopping stream.", _webcam.name());
                throw new IllegalStateException(_webcam.name() + ": stream stopped.");
            }
            try {
                final byte[] bytes = _webcam.takeImage();
                for (final WebcamStreamSubscriber subscriber : _subscriber) {
                    try {
                        subscriber.consume(bytes, _fps);
                    } catch (Exception e) {
                        _log.info("removing subscriber due to unexpected error.", e);
                        _subscriber.remove(subscriber);
                        _eventbus.send(new Webcamera.UnsubscribedStreamEvent(_webcam, subscriber));
                    }
                }
            } catch (Exception e) {
                _log.error("{}: webcam capture failed", _webcam.name(), e);
                _eventbus.send(new Webcamera.ClosedEvent(_webcam));
                throw new IllegalStateException(_webcam.name() + ": stream stopped.");
            }
        }

        @Override
        public void open() {
            _scheduler.scheduleAtFixedRate(this::process, 1000, _fps.getTime(), _fps.getTimeUnit());
        }

        @Override
        public void close() {
            _stopped.set(true);
            _subscriber.forEach(s -> _eventbus.send(new Webcamera.UnsubscribedStreamEvent(_webcam, s)));
        }

        @Override
        public void subscribe(final WebcamStreamSubscriber subscriber) {
            _subscriber.add(subscriber);
        }

        @Override
        public void unsubscribe(final WebcamStreamSubscriber subscriber) {
            _subscriber.remove(subscriber);
        }
    }
}
