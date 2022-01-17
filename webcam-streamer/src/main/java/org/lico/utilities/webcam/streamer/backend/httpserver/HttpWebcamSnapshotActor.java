package org.lico.utilities.webcam.streamer.backend.httpserver;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.lico.utilities.webcam.streamer.AbstractActorTyped;
import org.lico.utilities.webcam.streamer.backend.webcam.WebcameraActor;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.time.Duration;

public class HttpWebcamSnapshotActor extends AbstractActorTyped<HttpWebcamSnapshotActor.Command> {

    public interface Reply {

    }


    public interface Command {
        enum Initialize implements Command {
            It
        }

        final class HandleHttpRequest implements Command {
            public final HttpExchange exchange;

            public HandleHttpRequest(final HttpExchange exchange) {
                this.exchange = exchange;
            }
        }

        final class ImageTaken implements Command {
            public final WebcameraActor.Reply.ImageTaken reply;

            public ImageTaken(final WebcameraActor.Reply.ImageTaken reply) {
                this.reply = reply;
            }
        }
    }

    private final ActorRef<HttpServerActor.Command> _httpServer;
    private final WebcameraId _webcameraId;
    private final ActorRef<WebcameraActor.Command> _webcamera;
    private final ActorRef<WebcameraActor.Reply.ImageTaken> _webcamerImageTakenAdapter;
    private final HttpExchange _httpExchange;


    public HttpWebcamSnapshotActor(final ActorContext<Command> context, final ActorRef<HttpServerActor.Command> httpServer, final WebcameraId webcameraId, final ActorRef<WebcameraActor.Command> webcamera, final HttpExchange httpExchange) {
        super(context, context.getLog());
        _httpServer = httpServer;
        _webcameraId = webcameraId;
        _webcamera = webcamera;
        _webcamerImageTakenAdapter = context.messageAdapter(WebcameraActor.Reply.ImageTaken.class, Command.ImageTaken::new);
        _httpExchange = httpExchange;
        getContext().scheduleOnce(Duration.ofMillis(10), _webcamera, new WebcameraActor.Command.TakeImage(_webcamerImageTakenAdapter));
    }

    public static Behavior<HttpWebcamSnapshotActor.Command> create(final ActorRef<HttpServerActor.Command> httpServer, final WebcameraId webcameraId, final ActorRef<WebcameraActor.Command> webcamera, final HttpExchange httpExchange) {
        return Behaviors.setup(c -> new HttpWebcamSnapshotActor(c, httpServer, webcameraId, webcamera, httpExchange));
    }

    @Override
    public Receive<HttpWebcamSnapshotActor.Command> createReceive() {
        return newReceiveBuilder() //
                .onMessage(Command.Initialize.class, this::onInitialize)
                .onMessage(Command.ImageTaken.class, profile(this::onImageTaken))
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<Command> onInitialize(final Command.Initialize command) {
        _webcamera.tell(new WebcameraActor.Command.TakeImage(_webcamerImageTakenAdapter));
        return Behaviors.same();
    }

    private Behavior<Command> onImageTaken(final Command.ImageTaken command) {
        try {
            final Headers h = _httpExchange.getResponseHeaders();
            h.set("Content-Type", "image/jpeg");
            _httpExchange.sendResponseHeaders(200, 0);

            try (final OutputStream output = _httpExchange.getResponseBody()) {
                try (final ByteArrayInputStream image = new ByteArrayInputStream(command.reply.image)) {
                    image.transferTo(output);
                }
            }
        } catch (final Exception e) {
            return Behaviors.stopped();
        } finally {
            _httpExchange.close();
        }
        return Behaviors.stopped();
    }
}
