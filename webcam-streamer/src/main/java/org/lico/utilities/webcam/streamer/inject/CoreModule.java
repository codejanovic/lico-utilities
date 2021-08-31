package org.lico.utilities.webcam.streamer.inject;

import com.github.sarxos.webcam.Webcam;
import org.lico.utilities.webcam.streamer.server.WebServer;
import org.lico.utilities.webcam.streamer.settings.ApplicationSettings;
import org.lico.utilities.webcam.streamer.webcam.Webcameras;
import org.jusecase.inject.Injector;
import org.lico.utilities.webcamcapture.drivers.capturemanager.CaptureManagerDriver;

import java.util.function.Consumer;

public class CoreModule implements Consumer<Injector> {
    @Override
    public void accept(final Injector injector) {
        Webcam.setDriver(new CaptureManagerDriver());
        injector.add(new ApplicationSettings());
        injector.add(new Webcameras.Native());
        injector.add(new WebServer());
    }
}
