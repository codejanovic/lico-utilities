package org.lico.utilities.webcam.streamer.backend.settings;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.lico.utilities.webcam.streamer.AbstractActorTyped;
import org.lico.utilities.webcam.streamer.backend.BackendActor;
import org.lico.utilities.webcam.streamer.backend.webcam.WebcameraActor;
import org.lico.utilities.webcam.streamer.settings.ApplicationSettings;
import org.lico.utilities.webcam.streamer.settings.WebcamSettings;
import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SettingsActor extends AbstractActorTyped<SettingsActor.Command> {

    public interface Reply {

    }

    public interface Command {

        enum Initialize implements Command {
            It
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


    private static final ObjectMapper __mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    private static final String __userDir = System.getProperty("user.dir");
    private static final Path __settingsFilePath = Paths.get(__userDir, "config", "settings.yaml");
    private final ActorRef<BackendActor.Command> _backend;

    private ApplicationSettings _settings = new ApplicationSettings();

    public SettingsActor(final ActorContext<Command> context, final ActorRef<BackendActor.Command> backend) {
        super(context, context.getLog());
        _backend = backend;
    }

    public static Behavior<SettingsActor.Command> create(final ActorRef<BackendActor.Command> backend) {
        return Behaviors.setup(c -> new SettingsActor(c, backend));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder() //
                .onMessage(Command.Initialize.class, this::onInitialize) //
                .build();
    }

    private File getOrCreateSettingsFile() {
        try {
            final File settingsFile = new File(__settingsFilePath.toAbsolutePath().toString());
            if (!settingsFile.exists()) {
                settingsFile.getParentFile().mkdirs();
                settingsFile.createNewFile();
            }
            return settingsFile;
        } catch (final Exception e) {
            throw new IllegalStateException();
        }
    }

    private ApplicationSettings loadSettingsFromDisk() {
        try {
            final File settingsFile = getOrCreateSettingsFile();
            final boolean fileIsEmpty = settingsFile.length() == 0;
            if (fileIsEmpty) {
                return saveSettingsToDisk(settingsFile, new ApplicationSettings());
            }
            final ApplicationSettings settings = __mapper.readValue(settingsFile, ApplicationSettings.class);

            return settings.isValid() ? settings : saveSettingsToDisk(settingsFile, new ApplicationSettings());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

    }

    private ApplicationSettings saveSettingsToDisk(final File settingsFile, final ApplicationSettings settings) {
        try {
            __mapper.writeValue(settingsFile, settings);
            return settings;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void saveSettingsToDisk(final ApplicationSettings settings) {
        saveSettingsToDisk(getOrCreateSettingsFile(), settings);
    }

    private Behavior<Command> onInitialize(final Command.Initialize command) {
        _settings = loadSettingsFromDisk();
        _backend.tell(new BackendActor.Command.SettingsInitialized(_settings));
        return behaveInitialized();
    }

    public Behavior<Command> behaveInitialized() {
        return Behaviors.receive(Command.class) //
                .onMessage(Command.WebcamDiscovered.class, profile(this::onWebcamDiscovered)) //
                .onMessage(Command.WebcamOpened.class, profile(this::onWebcamOpened)) //
                .onMessage(Command.WebcamOpened.class, profile(this::onWebcamOpened)) //
                .onMessage(Command.WebcamClosed.class, profile(this::onWebcamClosed)) //
                .onMessage(Command.DriverChanged.class, profile(this::onDriverChanged)) //
                .onSignal(PreRestart.class, this::onBeforeStart) //
                .onSignal(PostStop.class, this::onBeforeKill) //
                .build();
    }

    private Behavior<Command> onWebcamDiscovered(final Command.WebcamDiscovered command) {
        _settings.findOrCreate(command.id);
        saveSettingsToDisk(_settings);
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamOpened(final Command.WebcamOpened command) {
        final WebcamSettings webcamSettings = _settings.findOrCreate(command.id);
        webcamSettings.setUsed(true);
        saveSettingsToDisk(_settings);
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamClosed(final Command.WebcamClosed command) {
        final WebcamSettings webcamSettings = _settings.findOrCreate(command.id);
        webcamSettings.setUsed(false);
        saveSettingsToDisk(_settings);
        return Behaviors.same();
    }

    private Behavior<Command> onDriverChanged(final Command.DriverChanged command) {
        _settings.setDriver(command.driver);
        saveSettingsToDisk(_settings);
        return Behaviors.same();
    }

    @Override
    protected Behavior<Command> onBeforeKill(final PostStop command) {
        saveSettingsToDisk(_settings);
        return super.onBeforeKill(command);
    }

}
