package org.lico.utilities.webcam.streamer.inject;

import org.jusecase.inject.Injector;
import org.lico.utilities.webcam.streamer.settings.ApplicationSettings;

import java.util.function.Consumer;

public class CoreModule implements Consumer<Injector> {
    @Override
    public void accept(final Injector injector) {
        injector.add(new ApplicationSettings());
    }
}
