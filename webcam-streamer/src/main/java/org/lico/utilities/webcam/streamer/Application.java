package org.lico.utilities.webcam.streamer;

import akka.actor.typed.ActorSystem;
import org.jusecase.inject.Injector;
import org.lico.utilities.webcam.streamer.inject.CoreModule;
import org.lico.utilities.webcam.streamer.inject.ExternalModule;
import org.lico.utilities.webcam.streamer.inject.SwingModule;


public class Application {

    private static final Injector _injector = Injector.getInstance();
    private ActorSystem<ApplicationActor.Command> _application;

    public static void main(final String[] args) {
        new ExternalModule().accept(_injector);
        new CoreModule().accept(_injector);
        new SwingModule().accept(_injector);

        final Application application;
        _injector.inject(application = new Application(), Application.class);
        application.start();
    }

    public void start() {
        if (_application != null) {
            return;
        }
        _application = ActorSystem.create(ApplicationActor.create(), "application");
        _application.tell(ApplicationActor.Command.Initialize.It);
    }
}
