package org.lico.utilities.webcam.streamer.ui;

import org.lico.utilities.webcam.streamer.webcam.Webcamera;
import org.apache.logging.log4j.Logger;
import org.jusecase.inject.Component;

import javax.inject.Inject;
import java.awt.*;
import java.net.URI;

@Component
public class WebcamSnapshotMenuItem extends MenuItem {
    private final Webcamera _webcam;
    @Inject
    private Logger _log;

    public WebcamSnapshotMenuItem(final Webcamera webcam) {
        super("Snapshot");
        _webcam = webcam;

        addActionListener(action -> {
            try {
                Desktop.getDesktop().browse(new URI(_webcam.urlToSSnapshot()));
            } catch (Exception e) {
                _log.error("unable to open browser", e);
            }
        });
    }
}
