package org.lico.utilities.webcam.streamer.backend.httpserver;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.sun.net.httpserver.HttpExchange;
import org.lico.utilities.webcam.streamer.AbstractActorTyped;
import org.lico.utilities.webcam.streamer.backend.webcam.WebcameraActor;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.UUID;

public class HttpWebcamActor extends AbstractActorTyped<HttpWebcamActor.Command> {

    public interface Command {
        enum Initialize implements Command {
            It
        }

        final class HandleHttpSnapshotRequest implements Command {
            public final HttpExchange httpRequest;

            public HandleHttpSnapshotRequest(final HttpExchange httpRequest) {
                this.httpRequest = httpRequest;
            }
        }

        final class HandleHttpStreamRequest implements Command {
            public final HttpExchange httpRequest;

            public HandleHttpStreamRequest(final HttpExchange httpRequest) {
                this.httpRequest = httpRequest;
            }
        }
    }

    private final ActorRef<HttpServerActor.Command> _httpServer;
    private final WebcameraId _webcameraId;
    private final ActorRef<WebcameraActor.Command> _webcamera;


    public HttpWebcamActor(final ActorContext<Command> context, final ActorRef<HttpServerActor.Command> httpServer, final WebcameraId webcameraId, final ActorRef<WebcameraActor.Command> webcamera) {
        super(context, context.getLog());
        _httpServer = httpServer;
        _webcameraId = webcameraId;
        _webcamera = webcamera;
    }

    public static Behavior<HttpWebcamActor.Command> create(final ActorRef<HttpServerActor.Command> httpServer, final WebcameraId webcameraId, final ActorRef<WebcameraActor.Command> webcamera) {
        return Behaviors.setup(c -> new HttpWebcamActor(c, httpServer, webcameraId, webcamera));
    }

    @Override
    public Receive<HttpWebcamActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Command.Initialize.class, profile(this::onInitialize)) //
                .build();
    }

    private Behavior<Command> onInitialize(final Command.Initialize command) {

        final String snapshotPath = String.format("%s/snapshot.jpg", _webcameraId.shortName());
        final String streamPath = String.format("%s/video.mjpeg", _webcameraId.shortName());
        _httpServer.tell(new HttpServerActor.Command.CreateHttpContext(
                snapshotPath,
                httpRequest -> {
                    self().tell(new Command.HandleHttpSnapshotRequest(httpRequest));
                }
        ));
        _httpServer.tell(new HttpServerActor.Command.CreateHttpContext(
                streamPath,
                httpRequest -> {
                    self().tell(new Command.HandleHttpStreamRequest(httpRequest));
                }
        ));
        return behaveInitialized();
    }

    public Behavior<HttpWebcamActor.Command> behaveInitialized() {
        return Behaviors.receive(Command.class) //
                .onMessage(Command.HandleHttpSnapshotRequest.class, this::handleHttpSnapshotRequest)
                .onMessage(Command.HandleHttpStreamRequest.class, this::handleHttpStreamRequest)
                .onSignal(PreRestart.class, this::onBeforeStart) //
                .onSignal(PostStop.class, this::onBeforeKill) //
                .build();
    }

    private Behavior<Command> handleHttpSnapshotRequest(final Command.HandleHttpSnapshotRequest command) {
        spawnWebserverRelatedActor(
                HttpWebcamSnapshotActor.create(_httpServer, _webcameraId, _webcamera, command.httpRequest)
                , "http-snapshot-handler-" + _webcameraId.arn() + "-" + UUID.randomUUID());
        return Behaviors.same();
    }

    private Behavior<Command> handleHttpStreamRequest(final Command.HandleHttpStreamRequest command) {
        spawnWebserverRelatedActor(
                HttpWebcamStreamActor.create(_httpServer, _webcameraId, _webcamera, command.httpRequest)
                , "http-stream-handler-" + _webcameraId.arn() + "-" + UUID.randomUUID());
        return Behaviors.same();
    }
}
