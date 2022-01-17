package org.lico.utilities.webcam.streamer;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.lico.utilities.webcam.streamer.backend.BackendActor;
import org.lico.utilities.webcam.streamer.frontend.FrontendActor;

public class ApplicationActor extends AbstractActorTyped<ApplicationActor.Command> {

    public interface Command {
        enum Initialize implements Command {
            It
        }

        enum Shutdown implements Command {
            It
        }
    }

    private final ActorRef<FrontendActor.Command> _frontend;
    private final ActorRef<BackendActor.Command> _backend;

    public ApplicationActor(final ActorContext<Command> context) {
        super(context, context.getLog());
        _backend = spawn(BackendActor.create(), "backend");
        _frontend = spawnSwingRelatedActor(FrontendActor.create(getContext().getSelf()), "frontend");
    }

    public static Behavior<ApplicationActor.Command> create() {
        return Behaviors.setup(ApplicationActor::new);
    }

    @Override
    public Receive<ApplicationActor.Command> createReceive() {
        return behaveUnitialized();
    }

    private Receive<Command> behaveUnitialized() {
        return newReceiveBuilder() //
                .onMessage(Command.Initialize.class, profile(this::onInitialize)) //
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Receive<Command> behaveInitialized() {
        return newReceiveBuilder() //
                .onMessage(Command.Shutdown.class, profile(this::onShutdown)) //
                .onSignal(PreRestart.class, profileSignal(this::onBeforeStart)) //
                .onSignal(PostStop.class, profileSignal(this::onBeforeKill)) //
                .build();
    }

    private Behavior<Command> onInitialize(final Command.Initialize command) {
        _backend.tell(new BackendActor.Command.Initialize(_frontend));
        _frontend.tell(new FrontendActor.Command.Initialize(_backend));
        return behaveInitialized();
    }

    private Behavior<Command> onShutdown(final Command.Shutdown command) {
        _backend.tell(BackendActor.Command.Shutdown.It);
        _frontend.tell(FrontendActor.Command.Shutdown.It);
        //TODO: wait for response of backend and frontend with timeout before exiting
        System.exit(0);
        return behaveUnitialized();
    }

}
