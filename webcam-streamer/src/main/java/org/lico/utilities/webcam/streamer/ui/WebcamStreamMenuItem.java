package org.lico.utilities.webcam.streamer.ui;

import org.lico.utilities.webcam.streamer.webcam.Webcamera;
import org.apache.logging.log4j.Logger;
import org.jusecase.inject.Component;

import javax.inject.Inject;
import java.awt.*;
import java.net.URI;

@Component
public class WebcamStreamMenuItem extends MenuItem {
    private final Webcamera _webcam;
    @Inject
    private Logger _log;

    public WebcamStreamMenuItem(final Webcamera webcam) {
        super("Mjpeg stream");
        _webcam = webcam;

        addActionListener(action -> {
            try {
                Desktop.getDesktop().browse(new URI(_webcam.urlToStream()));
            } catch (Exception e) {
                _log.error("unable to open browser", e);
            }
        });
    }
}
