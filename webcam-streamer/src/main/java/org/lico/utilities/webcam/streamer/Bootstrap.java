package org.lico.utilities.webcam.streamer;

import org.lico.utilities.webcam.streamer.inject.CoreModule;
import org.lico.utilities.webcam.streamer.inject.Eventbus;
import org.lico.utilities.webcam.streamer.inject.ExternalModule;
import org.lico.utilities.webcam.streamer.inject.SwingModule;
import org.jusecase.inject.Component;
import org.jusecase.inject.Injector;

import javax.inject.Inject;
import javax.swing.*;

@Component
public class Bootstrap {

    @Inject
    private Eventbus _eventbus;

    public static final String BOUNDARY = "123456789000000000000987654321";
    private static final Injector _injector = Injector.getInstance();

    public static void main(String[] args) {
        new ExternalModule().accept(_injector);
        new CoreModule().accept(_injector);
        new SwingModule().accept(_injector);

        final Bootstrap bootstrap;
        _injector.inject(bootstrap = new Bootstrap(), Bootstrap.class);
        bootstrap.start();
    }

    private void start() {
        SwingUtilities.invokeLater(() -> {
            try {
                _injector.inject(new TrayApplication(), TrayApplication.class);
            } catch (Exception ex) {
                System.out.println("Error - " + ex.getMessage());
            }
        });

    }


    public static final class ApplicationStartedEvent {

    }


}
