package org.lico.utilities.webcam.streamer.inject;

import org.jusecase.inject.Injector;

import java.util.function.Consumer;

public class ExternalModule implements Consumer<Injector> {
    @Override
    public void accept(final Injector injector) {
        injector.addProvider(new LoggerProvider());
    }
}
