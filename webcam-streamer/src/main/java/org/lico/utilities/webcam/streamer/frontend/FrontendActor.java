package org.lico.utilities.webcam.streamer.frontend;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.lico.utilities.webcam.streamer.AbstractActorTyped;
import org.lico.utilities.webcam.streamer.ApplicationActor;
import org.lico.utilities.webcam.streamer.backend.BackendActor;
import org.lico.utilities.webcam.streamer.backend.webcam.WebcameraActor;
import org.lico.utilities.webcam.streamer.frontend.component.DriverMenu;
import org.lico.utilities.webcam.streamer.frontend.component.WebcamerMenu;
import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FrontendActor extends AbstractActorTyped<FrontendActor.Command> {

    public interface Command {

        enum Shutdown implements Command {
            It
        }

        final class Initialize implements Command {

            public final ActorRef<BackendActor.Command> backend;

            public Initialize(final ActorRef<BackendActor.Command> backend) {
                this.backend = backend;
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
    }

    private static URL _logo;

    static {
        try {
            _logo = new URL("https://cdn4.iconfinder.com/data/icons/social-messaging-ui-color-squares-01/3/72-512.png");
        } catch (final MalformedURLException e) {
            _logo = null;
        }
    }

    private final ActorRef<ApplicationActor.Command> _application;
    private final Map<WebcameraId, ActorRef<WebcameraActor.Command>> _webcams = new HashMap<>();
    private ActorRef<BackendActor.Command> _backend;
    private TrayIcon _trayIcon;
    private PopupMenu _menuMain;
    private WebcamerMenu _menuWebcams;
    private DriverMenu _menuWebcamDrivers;

    public FrontendActor(final ActorContext<Command> context, final ActorRef<ApplicationActor.Command> application) {
        super(context, context.getLog());
        _application = application;
    }

    public static Behavior<Command> create(final ActorRef<ApplicationActor.Command> application) {
        return Behaviors.setup(c -> new FrontendActor(c, application));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder() //
                .onMessage(Command.Initialize.class, profile(this::onInitialize)) //
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<Command> behaveInitialized() {
        return Behaviors.receive(Command.class) //
                .onMessage(Command.WebcamDiscovered.class, profile(this::onWebcamDiscovered)) //
                .onMessage(Command.WebcamOpened.class, profile(this::onWebcamOpened)) //
                .onMessage(Command.WebcamClosed.class, profile(this::onWebcamClosed)) //
                .onMessage(Command.WebcamTerminated.class, profile(this::onWebcamTerminated)) //
                .onMessage(Command.DriverChanged.class, profile(this::onDriverChanged)) //
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<Command> onDriverChanged(final Command.DriverChanged command) {
        _menuWebcamDrivers.changeDriver(command.driver);
        return Behaviors.same();
    }


    private Behavior<Command> onWebcamDiscovered(final Command.WebcamDiscovered command) {
        _webcams.put(command.id, command.actor);
        _menuWebcams.addWebcam(command.id);
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamOpened(final Command.WebcamOpened command) {
        _menuWebcams.openWebcam(command.id);
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamClosed(final Command.WebcamClosed command) {
        _menuWebcams.closeWebcam(command.id);
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamTerminated(final Command.WebcamTerminated command) {
        _webcams.remove(command.id);
        _menuWebcams.removeWebcam(command.id);
        return Behaviors.same();
    }


    private Behavior<Command> onInitialize(final Command.Initialize command) {
        _backend = command.backend;
        buildUI();
        return behaveInitialized();
    }

    private void buildUI() {
        try {
            final BufferedImage trayIconImage = ImageIO.read(_logo);
            _trayIcon = new TrayIcon(trayIconImage.getScaledInstance(SystemTray.getSystemTray().getTrayIconSize().width, -1, Image.SCALE_SMOOTH));
            SystemTray.getSystemTray().add(_trayIcon);

            _trayIcon.setPopupMenu(null);
            _menuMain = new PopupMenu();

            final MenuItem menuExit = new MenuItem("Exit");
            menuExit.addActionListener(ae -> {
                _application.tell(ApplicationActor.Command.Shutdown.It);
            });

            _menuWebcamDrivers = new DriverMenu(driver
                    -> _backend.tell(new BackendActor.Command.DiscoverWebcams(driver)));

            _menuWebcams = new WebcamerMenu(
                    webcameraId -> _webcams.get(webcameraId).tell(WebcameraActor.Command.Open.It),
                    webcameraId -> _webcams.get(webcameraId).tell(WebcameraActor.Command.Close.It)
            );

            _menuMain.add(_menuWebcamDrivers);
            _menuMain.add(_menuWebcams);
            _menuMain.add(menuExit);

            _trayIcon.setPopupMenu(_menuMain);

        } catch (final Exception e) {
            _log.error("error upon application startup", e);
            throw new IllegalStateException(e);
        }
    }


}
