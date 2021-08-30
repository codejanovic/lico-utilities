package io.github.codejanovic.iot.webcam.streamer.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.github.codejanovic.iot.webcam.streamer.webcam.WebcamStreamSubscriber;
import io.github.codejanovic.iot.webcam.streamer.webcam.Webcamera;
import org.jusecase.inject.Component;

import java.io.OutputStream;
import java.util.function.Function;

import static io.github.codejanovic.iot.webcam.streamer.Bootstrap.BOUNDARY;

@Component
public class WebcamMjpegHandler extends WebcamHandler {
    public WebcamMjpegHandler(final Webcamera webcam, final Function<OutputStream, WebcamStreamSubscriber> consumerFactory) {
        super(webcam, consumerFactory);
    }

    @Override
    protected void before(final HttpExchange exchange) {
        Headers h = exchange.getResponseHeaders();
        h.set("Content-Type", "multipart/x-mixed-replace; boundary=" + BOUNDARY);
    }
}
