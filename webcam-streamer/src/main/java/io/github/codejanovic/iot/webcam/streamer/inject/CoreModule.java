package io.github.codejanovic.iot.webcam.streamer.inject;

import com.github.eduramiba.webcamcapture.drivers.capturemanager.CaptureManagerDriver;
import com.github.sarxos.webcam.Webcam;
import io.github.codejanovic.iot.webcam.streamer.server.WebServer;
import io.github.codejanovic.iot.webcam.streamer.settings.ApplicationSettings;
import io.github.codejanovic.iot.webcam.streamer.webcam.Webcameras;
import org.jusecase.inject.Injector;

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
