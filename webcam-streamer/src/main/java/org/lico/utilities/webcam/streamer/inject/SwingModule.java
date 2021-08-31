package org.lico.utilities.webcam.streamer.inject;

import org.jusecase.inject.Injector;

import java.util.function.Consumer;

public class SwingModule implements Consumer<Injector> {
    @Override
    public void accept(final Injector injector) {
        System.setProperty("apple.awt.UIElement", "true");
    }
}
