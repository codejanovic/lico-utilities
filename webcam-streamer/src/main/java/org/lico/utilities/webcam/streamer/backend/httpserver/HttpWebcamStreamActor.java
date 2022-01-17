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
import org.lico.utilities.webcam.streamer.webcam.FramesPerSecond;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.io.OutputStream;
import java.time.Duration;

public class HttpWebcamStreamActor extends AbstractActorTyped<HttpWebcamStreamActor.Command> {

    public interface Reply {

    }


    public interface Command {
        enum Initialize implements Command {
            It
        }

        final class ImageTaken implements Command {
            public final WebcameraActor.Reply.ImageTaken reply;

            public ImageTaken(final WebcameraActor.Reply.ImageTaken reply) {
                this.reply = reply;
            }
        }
    }

    private static final String NL = "\r\n";
    private static final String BOUNDARY = "http-webcam-stream-boundary";
    private static final String HEAD = NL + NL + "--" + BOUNDARY + NL +
            "Content-Type: image/jpeg" + NL +
            "Content-Length: ";

    private final ActorRef<HttpServerActor.Command> _httpServer;
    private final WebcameraId _webcameraId;
    private final ActorRef<WebcameraActor.Command> _webcamera;
    private final ActorRef<WebcameraActor.Reply.ImageTaken> _webcamImageTakenAdapter;
    private final HttpExchange _httpExchange;
    private final OutputStream _httpResponse;


    public HttpWebcamStreamActor(final ActorContext<Command> context, final ActorRef<HttpServerActor.Command> httpServer, final WebcameraId webcameraId, final ActorRef<WebcameraActor.Command> webcamera, final HttpExchange httpExchange) {
        super(context, context.getLog());
        _httpServer = httpServer;
        _webcameraId = webcameraId;
        _webcamera = webcamera;
        _httpExchange = httpExchange;
        _httpResponse = httpExchange.getResponseBody();
        _webcamImageTakenAdapter = context.messageAdapter(WebcameraActor.Reply.ImageTaken.class, Command.ImageTaken::new);
        getContext().scheduleOnce(Duration.ofMillis(10), self(), Command.Initialize.It);
    }

    public static Behavior<Command> create(final ActorRef<HttpServerActor.Command> httpServer, final WebcameraId webcameraId, final ActorRef<WebcameraActor.Command> webcamera, final HttpExchange httpExchange) {
        return Behaviors.setup(c -> new HttpWebcamStreamActor(c, httpServer, webcameraId, webcamera, httpExchange));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Command.Initialize.class, this::onInitialize)
                .onMessage(Command.ImageTaken.class, this::onImageTaken)
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<Command> onInitialize(final Command.Initialize command) {
        try {
            final Headers h = _httpExchange.getResponseHeaders();
            h.set("Cache-Control", "no-cache, private");
            h.set("Content-Type", "multipart/x-mixed-replace;boundary=" + BOUNDARY);
            _httpExchange.sendResponseHeaders(200, 0);
            getContext().scheduleOnce(Duration.ofMillis(10), _webcamera, new WebcameraActor.Command.TakeImage(_webcamImageTakenAdapter));
        } catch (final Exception e) {
            return Behaviors.stopped();
        }
        return Behaviors.same();
    }

    private Behavior<Command> onImageTaken(final Command.ImageTaken command) {
        try {
            _httpResponse.write((HEAD + command.reply.image.length + NL + NL).getBytes());
            _httpResponse.write(command.reply.image);
            _httpResponse.write((NL + NL).getBytes());
            _httpResponse.flush();

            getContext().scheduleOnce(FramesPerSecond.FPS_10.getDuration(), _webcamera, new WebcameraActor.Command.TakeImage(_webcamImageTakenAdapter));
            return Behaviors.same();
        } catch (final Exception e) {
            try {
                _httpResponse.close();
                _httpExchange.close();
                return Behaviors.stopped();
            } catch (final Exception e1) {
                return Behaviors.stopped();
            }
        }
    }
}
