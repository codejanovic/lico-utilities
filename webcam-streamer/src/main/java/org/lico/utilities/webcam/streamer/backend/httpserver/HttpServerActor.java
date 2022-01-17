package org.lico.utilities.webcam.streamer.backend.httpserver;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.lico.utilities.webcam.streamer.AbstractActorTyped;
import org.lico.utilities.webcam.streamer.backend.webcam.WebcameraActor;
import org.lico.utilities.webcam.streamer.settings.WebServerSettings;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServerActor extends AbstractActorTyped<HttpServerActor.Command> {

    public interface Command {

        final class Initialize implements Command {

            private final WebServerSettings settings;

            public Initialize(final WebServerSettings settings) {
                this.settings = settings;
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

        final class HttpWebcamTerminated implements Command {

            private final WebcameraId _webcamerId;

            public HttpWebcamTerminated(final WebcameraId webcamerId) {

                _webcamerId = webcamerId;
            }
        }

        final class RemoveHttpContext implements Command {
            public final String path;

            public RemoveHttpContext(final String path) {
                this.path = path;
            }
        }

        final class CreateHttpContext implements Command {

            public final String path;
            public final HttpHandler handler;

            public CreateHttpContext(final String path, final HttpHandler handler) {
                this.path = path;
                this.handler = handler;
            }
        }
    }

    private final ExecutorService _httpThreadPool = Executors.newFixedThreadPool(10);
    private final Map<WebcameraId, ActorRef<HttpWebcamActor.Command>> _httpWebcams = new HashMap<>();
    private HttpServer _httpServer;

    public HttpServerActor(final ActorContext<HttpServerActor.Command> context) {
        super(context, context.getLog());
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(HttpServerActor::new);
    }

    @Override
    public Receive<HttpServerActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Command.Initialize.class, profile(this::onInitialize)) //
                .build();
    }

    public Behavior<Command> behaveInitialized() {
        return Behaviors.receive(Command.class) //
                .onMessage(Command.WebcamOpened.class, profile(this::onWebcamOpened)) //
                .onMessage(Command.WebcamClosed.class, profile(this::onWebcamClosed)) //
                .onMessage(Command.WebcamTerminated.class, profile(this::onWebcamTerminated)) //
                .onMessage(Command.HttpWebcamTerminated.class, profile(this::onHttpWebcamTerminated)) //
                .onMessage(Command.CreateHttpContext.class, profile(this::onCreateHttpContext)) //
                .onMessage(Command.RemoveHttpContext.class, profile(this::onRemoveHttpContext)) //
                .onSignal(PreRestart.class, this::onBeforeStart) //
                .onSignal(PostStop.class, this::onBeforeKill) //
                .build();
    }

    private Behavior<Command> onCreateHttpContext(final Command.CreateHttpContext command) {
        _log.info("path http://localhost:{}/{}: creating http context", _httpServer.getAddress().getPort(), command.path);
        _httpServer.createContext("/" + command.path, command.handler);
        return Behaviors.same();
    }

    private Behavior<Command> onRemoveHttpContext(final Command.RemoveHttpContext command) {
        _log.info("path http://localhost:{}/{}: removing http context", _httpServer.getAddress().getPort(), command.path);
        _httpServer.removeContext("/" + command.path);
        return Behaviors.same();
    }

    private Behavior<Command> onInitialize(final Command.Initialize command) {
        startServer(command.settings);
        return behaveInitialized();
    }

    private void startServer(final WebServerSettings settings) {
        try {
            _httpServer = HttpServer.create(new InetSocketAddress(settings.getPort()), 0);

            _httpServer.createContext("/", (rootHandler) -> {
                final byte[] response = "<!DOCTYPE html><html><body>Server is up and running</body></html>".getBytes();
                rootHandler.sendResponseHeaders(200, response.length);
                final OutputStream os = rootHandler.getResponseBody();
                os.write(response);
                os.flush();
                rootHandler.close();
            });

            _httpServer.setExecutor(null);
            _httpServer.start();

        } catch (final IOException e) {
            throw new IllegalStateException("unable to start server with port " + settings.getPort(), e);
        }
    }

    private Behavior<Command> onWebcamTerminated(final Command.WebcamTerminated command) {
        final ActorRef<HttpWebcamActor.Command> httpWebcamActor = _httpWebcams.get(command.id);
        if (httpWebcamActor == null) {
            return Behaviors.same();
        }
        stopActor(_httpWebcams.remove(command.id));
        return Behaviors.same();
    }

    private Behavior<Command> onHttpWebcamTerminated(final Command.HttpWebcamTerminated command) {
        _httpWebcams.remove(command._webcamerId);
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamOpened(final Command.WebcamOpened command) {
        final ActorRef<HttpWebcamActor.Command> httpWebcamActor = spawnWebserverRelatedActor(HttpWebcamActor.create(self(), command.id, command.actor), "http-" + command.id.arn());
        getContext().watchWith(httpWebcamActor, new Command.HttpWebcamTerminated(command.id));
        _httpWebcams.put(command.id, httpWebcamActor);
        httpWebcamActor.tell(HttpWebcamActor.Command.Initialize.It);
        return Behaviors.same();
    }

    private Behavior<Command> onWebcamClosed(final Command.WebcamClosed command) {
        final ActorRef<HttpWebcamActor.Command> httpWebcamActor = _httpWebcams.get(command.id);
        if (httpWebcamActor == null) {
            return Behaviors.same();
        }
        stopActor(_httpWebcams.remove(command.id));
        return Behaviors.same();
    }

}
