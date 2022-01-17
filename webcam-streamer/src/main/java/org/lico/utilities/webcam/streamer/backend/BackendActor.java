package org.lico.utilities.webcam.streamer.backend;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.lico.utilities.webcam.streamer.AbstractActorTyped;
import org.lico.utilities.webcam.streamer.backend.httpserver.HttpServerActor;
import org.lico.utilities.webcam.streamer.backend.settings.SettingsActor;
import org.lico.utilities.webcam.streamer.backend.webcam.WebcameraActor;
import org.lico.utilities.webcam.streamer.backend.webcam.WebcamerasActor;
import org.lico.utilities.webcam.streamer.frontend.FrontendActor;
import org.lico.utilities.webcam.streamer.settings.ApplicationSettings;
import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;


public class BackendActor extends AbstractActorTyped<BackendActor.Command> {

    public interface Reply {
    }

    public interface Command {

        enum Shutdown implements Command {
            It
        }

        final class Initialize implements Command {

            public final ActorRef<FrontendActor.Command> frontend;

            public Initialize(final ActorRef<FrontendActor.Command> frontend) {
                this.frontend = frontend;
            }

        }

        final class WebcamTerminated implements Command {
            public final ActorRef<WebcameraActor.Command> actor;
            public final WebcameraId id;

            public WebcamTerminated(final ActorRef<WebcameraActor.Command> actor, final WebcameraId id) {
                this.actor = actor;
                this.id = id;
            }
        }

        final class WebcamDiscovered implements Command {

            public final ActorRef<WebcameraActor.Command> actor;
            public final WebcameraId id;

            public WebcamDiscovered(final ActorRef<WebcameraActor.Command> actor, final WebcameraId id) {
                this.actor = actor;
                this.id = id;
            }
        }

        final class WebcamOpened implements Command {
            public final ActorRef<WebcameraActor.Command> actor;
            public final WebcameraId id;

            public WebcamOpened(final ActorRef<WebcameraActor.Command> actor, final WebcameraId id) {
                this.actor = actor;
                this.id = id;
            }
        }

        final class WebcamClosed implements Command {
            public final ActorRef<WebcameraActor.Command> actor;
            public final WebcameraId id;

            public WebcamClosed(final ActorRef<WebcameraActor.Command> actor, final WebcameraId id) {
                this.actor = actor;
                this.id = id;
            }
        }

        final class DriverChanged implements Command {
            public final WebcameraDriver driver;

            public DriverChanged(final WebcameraDriver driver) {
                this.driver = driver;
            }
        }

        final class SettingsInitialized implements Command {
            public final ApplicationSettings applicationSettings;

            public SettingsInitialized(final ApplicationSettings applicationSettings) {
                this.applicationSettings = applicationSettings;
            }
        }

        final class DiscoverWebcams implements Command {
            public final WebcameraDriver driver;

            public DiscoverWebcams(final WebcameraDriver driver) {
                this.driver = driver;
            }
        }

    }

    private final ActorRef<SettingsActor.Command> _settings;
    private final ActorRef<HttpServerActor.Command> _httpServer;
    private final ActorRef<WebcamerasActor.Command> _webcameras;
    private ActorRef<FrontendActor.Command> _frontend;

    public BackendActor(final ActorContext<Command> context) {
        super(context, context.getLog());
        _settings = spawnFilesystemRelatedActor(SettingsActor.create(self()), "settings");
        _webcameras = spawnWebcamRelatedActor(WebcamerasActor.create(self()), "webcameras");
        _httpServer = spawnWebserverRelatedActor(HttpServerActor.create(), "webserver");
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(BackendActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Command.Initialize.class, profile(this::onInitialize)) //
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<Command> behaveInitialized() {
        return Behaviors.receive(Command.class) //
                .onMessage(Command.DiscoverWebcams.class, profile(this::onDiscover)) //
                .onMessage(Command.WebcamDiscovered.class, profile(this::onWebcamDiscovered)) //
                .onMessage(Command.WebcamOpened.class, profile(this::onWebcamOpened)) //
                .onMessage(Command.WebcamClosed.class, profile(this::onWebcamClosed)) //
                .onMessage(Command.DriverChanged.class, profile(this::onDriverChanged)) //
                .onMessage(Command.SettingsInitialized.class, profile(this::onSettingsInitialized)) //
                .onMessage(Command.WebcamTerminated.class, profile(this::onWebcamTerminated)) //
                .onMessage(Command.Shutdown.class, profile(this::onShutdown)) //
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<Command> onWebcamDiscovered(final Command.WebcamDiscovered command) {
        _frontend.tell(new FrontendActor.Command.WebcamDiscovered(command.actor, command.id));
        _settings.tell(new SettingsActor.Command.WebcamDiscovered(command.actor, command.id));
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamOpened(final Command.WebcamOpened command) {
        _frontend.tell(new FrontendActor.Command.WebcamOpened(command.actor, command.id));
        _settings.tell(new SettingsActor.Command.WebcamOpened(command.actor, command.id));
        _httpServer.tell(new HttpServerActor.Command.WebcamOpened(command.actor, command.id));
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamClosed(final Command.WebcamClosed command) {
        _frontend.tell(new FrontendActor.Command.WebcamClosed(command.actor, command.id));
        _settings.tell(new SettingsActor.Command.WebcamClosed(command.actor, command.id));
        _httpServer.tell(new HttpServerActor.Command.WebcamClosed(command.actor, command.id));
        return Behaviors.same();
    }

    private Behavior<Command> onDriverChanged(final Command.DriverChanged command) {
        _frontend.tell(new FrontendActor.Command.DriverChanged(command.driver));
        _settings.tell(new SettingsActor.Command.DriverChanged(command.driver));
        return Behaviors.same();
    }

    private Behavior<Command> onSettingsInitialized(final Command.SettingsInitialized command) {
        _httpServer.tell(new HttpServerActor.Command.Initialize(command.applicationSettings.server));
        _webcameras.tell(new WebcamerasActor.Command.Initialize(command.applicationSettings.driver, command.applicationSettings.webcams));
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamTerminated(final Command.WebcamTerminated command) {
        _frontend.tell(new FrontendActor.Command.WebcamTerminated(command.actor, command.id));
        _httpServer.tell(new HttpServerActor.Command.WebcamTerminated(command.actor, command.id));
        return Behaviors.same();
    }


    private Behavior<Command> onInitialize(final Command.Initialize command) {
        _frontend = command.frontend;
        _settings.tell(SettingsActor.Command.Initialize.It);
        return behaveInitialized();
    }

    private Behavior<Command> onDiscover(final Command.DiscoverWebcams command) {
        _webcameras.tell(new WebcamerasActor.Command.Discover(command.driver));
        _settings.tell(new SettingsActor.Command.DriverChanged(command.driver));
        _frontend.tell(new FrontendActor.Command.DriverChanged(command.driver));
        return Behaviors.same();
    }

    private Behavior<Command> onShutdown(final Command.Shutdown command) {
        return Behaviors.empty();
    }
}
