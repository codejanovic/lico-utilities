package org.lico.utilities.webcam.streamer.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.lico.utilities.webcam.streamer.webcam.WebcamStreamSubscriber;
import org.lico.utilities.webcam.streamer.webcam.Webcamera;
import org.jusecase.inject.Component;

import java.io.OutputStream;
import java.util.function.Function;

@Component
public class WebcamSnapshotHandler extends WebcamHandler {
    public WebcamSnapshotHandler(final Webcamera webcam, final Function<OutputStream, WebcamStreamSubscriber> consumerFactory) {
        super(webcam, consumerFactory);
    }

    @Override
    protected void before(final HttpExchange exchange) {
        Headers h = exchange.getResponseHeaders();
        h.set("Content-Type", "image/jpeg");
    }
}