package org.lico.utilities.webcam.streamer.webcam;

import com.github.sarxos.webcam.Webcam;
import org.lico.utilities.webcam.streamer.Bootstrap;
import org.lico.utilities.webcam.streamer.inject.Eventbus;
import org.lico.utilities.webcam.streamer.server.WebServer;
import org.lico.utilities.webcam.streamer.settings.ApplicationSettings;
import org.lico.utilities.webcam.streamer.settings.WebcamSettings;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.Subscribe;
import org.jusecase.inject.Component;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public interface Webcameras {

    Collection<Webcamera> all();

    final class WebcamsInitiatedEvent {
        public final Set<Webcamera> all = ConcurrentHashMap.newKeySet();
        public final Set<Webcamera> removed = ConcurrentHashMap.newKeySet();
    }

    @Component
    final class Native implements Webcameras {
        private final Set<Webcamera> _webcams = ConcurrentHashMap.newKeySet();

        @Inject
        private Logger              _log;
        @Inject
        private Eventbus            _eventbus;
        @Inject
        private ApplicationSettings _settings;

        public Native() {
            _eventbus.register(this);
        }

        @Subscribe
        public void onApplicationStarted(final Bootstrap.ApplicationStartedEvent ignore) {
            _log.info("Received {} event, will init Webcams ...", ignore.getClass().getSimpleName());
            _settings.load();
            final WebcamsInitiatedEvent initiatedEvent = new WebcamsInitiatedEvent();
            final Set<Webcamera> webcams = Webcam.getWebcams().stream() //
                    .map(Webcamera.Native::new) //
                    .peek(w -> _log.info("{}: initiliazing webcam", w.name())) //
                    .collect(Collectors.toSet());

            initiatedEvent.all.addAll(webcams);
            initiatedEvent.removed.addAll(_webcams);
            initiatedEvent.removed.removeAll(webcams);

            _webcams.clear();
            _webcams.addAll(webcams);

            for (Webcamera webcam : _webcams) {
                final WebcamSettings camSettings = _settings.findOrCreate(webcam);
                if (camSettings.isUsed()) {
                    _log.info("{}: opening webcam", webcam.name());
                    webcam.open();
                }
            }
            _settings.save();
            _eventbus.send(initiatedEvent);
            _eventbus.send(new WebServer.RestartEvent());
            _log.info("Received {} event, will init Webcams ... DONE", ignore.getClass().getSimpleName());
        }

        @Override
        public Collection<Webcamera> all() {
            return new HashSet<>(_webcams);
        }
    }
}
