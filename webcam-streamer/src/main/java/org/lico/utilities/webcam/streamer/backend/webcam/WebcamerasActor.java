package org.lico.utilities.webcam.streamer.backend.webcam;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.github.sarxos.webcam.Webcam;
import org.lico.utilities.webcam.streamer.AbstractActorTyped;
import org.lico.utilities.webcam.streamer.backend.BackendActor;
import org.lico.utilities.webcam.streamer.settings.WebcamSettings;
import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class WebcamerasActor extends AbstractActorTyped<WebcamerasActor.Command> {


    public interface Reply {

    }

    public interface Command {

        enum Shutdown implements Command {
            It
        }

        final class Initialize implements Command {

            private final WebcameraDriver driver;
            private final List<WebcamSettings> webcams;

            public Initialize(final WebcameraDriver driver, final List<WebcamSettings> webcams) {

                this.driver = driver;
                this.webcams = webcams;
            }
        }

        final class Discover implements Command {
            public final WebcameraDriver driver;

            public Discover(final WebcameraDriver driver) {
                this.driver = driver;
            }
        }
    }

    private final Map<WebcameraId, ActorRef<WebcameraActor.Command>> _webcams = new HashMap<>();
    private final ActorRef<BackendActor.Command> _backend;

    public WebcamerasActor(final ActorContext<WebcamerasActor.Command> context, final ActorRef<BackendActor.Command> backend) {
        super(context, context.getLog());
        _backend = backend;
    }

    public static Behavior<Command> create(final ActorRef<BackendActor.Command> backend) {
        return Behaviors.setup(c -> new WebcamerasActor(c, backend));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder() //
                .onMessage(Command.Initialize.class, this::onInitialize) //
                .onSignal(PreRestart.class, this::onBeforeStart) //
                .onSignal(PostStop.class, this::onBeforeKill) //
                .build();
    }


    private Behavior<Command> behaveInitialized() {
        return Behaviors.receive(Command.class) //
                .onMessage(Command.Discover.class, this::onDiscover) //
                .onMessage(Command.Shutdown.class, this::onShutdown) //
                .onSignal(PreRestart.class, this::onBeforeStart) //
                .onSignal(PostStop.class, this::onBeforeKill) //
                .build();
    }

    private Behavior<Command> onShutdown(final Command.Shutdown command) {
        _webcams.values().forEach(webcam -> webcam.tell(WebcameraActor.Command.Shutdown.It));
        return Behaviors.same();
    }

    private Behavior<Command> onDiscover(final Command.Discover command) {
        rediscover(command.driver, List.of());
        return Behaviors.same();
    }

    private void resetDriver(final WebcameraDriver driver) {
        _log.info("changing webcam driver from {} to {}", Webcam.getDriver().getClass().getSimpleName(), driver.get().getClass().getSimpleName());
        Webcam.setDriver(driver.get());
    }

    private void rediscover(final WebcameraDriver driver, final Collection<WebcamSettings> webcamSettings) {
        final Map<String, WebcamSettings> webcamSettingsById = webcamSettings.stream().collect(Collectors.toMap(ws -> ws.identifier, v -> v));

        _webcams.values().forEach(this::stopActor);
        _webcams.clear();

        resetDriver(driver);

        for (final Webcam webcam : Webcam.getWebcams()) {
            final WebcameraId webcameraId = new WebcameraId.Unique(webcam);
            final ActorRef<WebcameraActor.Command> webcamActor = spawnWebcamRelatedActor(WebcameraActor.create(webcam, _backend), new WebcameraId.Unique(webcam).arn());
            _log.info("discovered webcam {} ({})", webcameraId.name(), webcameraId.shortName());
            _webcams.put(webcameraId, webcamActor);
        }
        _log.info("discovered webcams (driver: {}): {}", driver, _webcams.size());

        _backend.tell(new BackendActor.Command.DriverChanged(driver));
        _webcams.forEach((id, webcam) -> {
            webcam.tell(WebcameraActor.Command.Initialize.It);
            if (!webcamSettingsById.containsKey(id.identifier())) {
                return;
            }
            final WebcamSettings webcamSetting = webcamSettingsById.get(id.identifier());
            if (!webcamSetting.autostart) {
                return;
            }
            webcam.tell(WebcameraActor.Command.Open.It);
        });
    }

    private Behavior<Command> onInitialize(final Command.Initialize command) {
        rediscover(command.driver, command.webcams);
        return behaveInitialized();
    }
}
