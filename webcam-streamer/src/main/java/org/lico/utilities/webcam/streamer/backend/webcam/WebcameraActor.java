package org.lico.utilities.webcam.streamer.backend.webcam;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamUtils;
import org.lico.utilities.webcam.streamer.AbstractActorTyped;
import org.lico.utilities.webcam.streamer.backend.BackendActor;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.Arrays;
import java.util.Comparator;


public class WebcameraActor extends AbstractActorTyped<WebcameraActor.Command> {

    public interface Reply {

        final class ImageTaken implements Reply {
            public final byte[] image;

            public ImageTaken(final byte[] image) {
                this.image = image;
            }
        }
    }


    public interface Command {

        enum Initialize implements Command {
            It
        }

        enum Shutdown implements Command {
            It
        }

        enum Open implements Command {
            It
        }

        enum Close implements Command {
            It
        }

        final class TakeImage implements Command {
            public final ActorRef<Reply.ImageTaken> replyTo;

            public TakeImage(final ActorRef<Reply.ImageTaken> replyTo) {
                this.replyTo = replyTo;
            }
        }
    }

    private final ActorRef<BackendActor.Command> _backend;
    private final Webcam _webcamera;
    private final WebcameraId _webcameraId;

    public WebcameraActor(final ActorContext<Command> context, final Webcam webcamera, final ActorRef<BackendActor.Command> backend) {
        super(context, context.getLog());
        _webcamera = webcamera;
        _backend = backend;
        _webcameraId = new WebcameraId.Unique(_webcamera);
    }

    public static Behavior<Command> create(final Webcam webcamera, final ActorRef<BackendActor.Command> backend) {
        return Behaviors.setup(c -> new WebcameraActor(c, webcamera, backend));
    }

    @Override
    public Receive<WebcameraActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Command.Initialize.class, profile(this::onInitialize)) //
                .build();
    }

    private Behavior<WebcameraActor.Command> behaveClosed() {
        return Behaviors.receive(Command.class) //
                .onMessage(Command.Open.class, profile(this::onOpen)) //
                .onMessage(Command.Shutdown.class, profile(this::onShutdown)) //
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<WebcameraActor.Command> behaveOpened() {
        return Behaviors.receive(Command.class) //
                .onMessage(Command.Close.class, profile(this::onClose)) //
                .onMessage(Command.TakeImage.class, this::onTakeImage) //
                .onMessage(Command.Shutdown.class, profile(this::onShutdown)) //
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<Command> onTakeImage(final Command.TakeImage command) {
        command.replyTo.tell(new Reply.ImageTaken(WebcamUtils.getImageBytes(_webcamera, "jpg")));
        return Behaviors.same();
    }

    private void closeWebcam() {
        _webcamera.close();
    }

    @Override
    protected Behavior<Command> onBeforeKill(final PostStop command) {
        closeWebcam();
        _backend.tell(new BackendActor.Command.WebcamTerminated(self(), _webcameraId));
        return super.onBeforeKill(command);
    }

    private Behavior<Command> onClose(final Command.Close command) {
        closeWebcam();
        _backend.tell(new BackendActor.Command.WebcamClosed(self(), _webcameraId));
        return behaveClosed();
    }

    private Behavior<Command> onOpen(final Command.Open command) {
        openWebcam();
        _backend.tell(new BackendActor.Command.WebcamOpened(self(), _webcameraId));
        return behaveOpened();
    }

    private Behavior<Command> onShutdown(final Command.Shutdown command) {
        return Behaviors.stopped();
    }

    private Behavior<Command> onInitialize(final Command.Initialize command) {
        _backend.tell(new BackendActor.Command.WebcamDiscovered(self(), _webcameraId));
        return behaveClosed();
    }


    private void openWebcam() {
        Arrays.stream(_webcamera.getViewSizes())
                .max(Comparator.comparingInt(v -> v.height))
                .ifPresent(_webcamera::setViewSize);
        _log.info("{}: opening camera with dimensions of {}", _webcamera.getName(), _webcamera.getViewSize());
        _webcamera.open();
    }


}
