package org.lico.utilities.webcam.streamer.inject;

import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.jusecase.inject.Component;

import javax.inject.Inject;

public interface Eventbus {
    void send(Object event);

    void register(Object susbcriber);

    @Component
    final class GreenRobotBus implements Eventbus {
        @Inject
        private Logger _log;
        private final EventBus _greenRobotBus = EventBus.getDefault();

        @Override
        public void send(final Object event) {
            _log.info("Eventbus: sending event {}", event.getClass().getSimpleName());
            _greenRobotBus.post(event);
        }

        @Override
        public void register(final Object subscriber) {
            _log.info("Eventbus: subscribing {}", subscriber.getClass().getSimpleName());
            _greenRobotBus.register(subscriber);
        }
    }
}
