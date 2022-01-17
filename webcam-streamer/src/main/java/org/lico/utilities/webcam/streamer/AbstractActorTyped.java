package org.lico.utilities.webcam.streamer;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import org.lico.utilities.webcam.streamer.backend.Dispatchers;
import org.slf4j.Logger;

import java.time.Duration;


public abstract class AbstractActorTyped<T> extends AbstractBehavior<T> {

    protected final Logger _log;
    protected final ActorContext<T> _context;

    public AbstractActorTyped(final ActorContext<T> context, final Logger log) {
        super(context);
        _context = getContext();
        _log = log;
    }

    protected <Req, Res> void ask(final Class<Res> resClass, final RecipientRef<Req> target, final Duration responseTimeout,
                                  final Function<ActorRef<Res>, Req> createRequest, final Function2<Res, Throwable, T> applyToResponse) {
        _context.ask(resClass, target, responseTimeout, createRequest, applyToResponse);
    }

    protected <T> void stopActor(final ActorRef<T> child) {
        _context.stop(child);
        _context.stop(child);
    }

    protected void discoverableBy(final ServiceKey<T> key) {
        _context.getSystem().receptionist().tell(Receptionist.register(key, self()));
    }

    protected ActorRef<T> self() {
        return _context.getSelf();
    }

    protected <U> ActorRef<U> spawn(final Behavior<U> behavior, final String name) {
        return _context.spawn(behavior, name);
    }

    protected <U> ActorRef<U> spawnFilesystemRelatedActor(final Behavior<U> behavior, final String name) {
        return _context.spawn(behavior, name, new Dispatchers().filesystem());
    }

    protected <U> ActorRef<U> spawnSwingRelatedActor(final Behavior<U> behavior, final String name) {
        return _context.spawn(behavior, name, new Dispatchers().swing());
    }

    protected <U> ActorRef<U> spawnWebcamRelatedActor(final Behavior<U> behavior, final String name) {
        return _context.spawn(behavior, name, new Dispatchers().webcams());
    }

    protected <U> ActorRef<U> spawnWebserverRelatedActor(final Behavior<U> behavior, final String name) {
        return _context.spawn(behavior, name, new Dispatchers().webserver());
    }


    protected <R extends T> Function<R, Behavior<T>> profile(final java.util.function.Function<R, Behavior<T>> behavior) {
        return command -> {
            _log.info("Received {} command", command.getClass().getSimpleName());
            return behavior.apply(command);
        };
    }

    protected <R extends Signal> Function<R, Behavior<T>> profileSignal(final java.util.function.Function<R, Behavior<T>> behavior) {
        return command -> {
            _log.info("Received {} signal", command.getClass().getSimpleName());
            return behavior.apply(command);
        };
    }


    protected Behavior<T> onBeforeKill(final PostStop command) {
        return this;
    }

    protected Behavior<T> onBeforeStart(final PreRestart command) {
        return this;
    }

}
