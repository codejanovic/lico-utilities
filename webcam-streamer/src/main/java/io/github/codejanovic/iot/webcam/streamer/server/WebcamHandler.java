package io.github.codejanovic.iot.webcam.streamer.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.codejanovic.iot.webcam.streamer.webcam.WebcamStreamSubscriber;
import io.github.codejanovic.iot.webcam.streamer.webcam.Webcamera;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jusecase.inject.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

@Component
public abstract class WebcamHandler implements HttpHandler {
    private static final Logger _log = LogManager.getLogger(WebcamHandler.class);

    private final Webcamera _webcam;
    private final Function<OutputStream, WebcamStreamSubscriber> _consumerFactory;

    public WebcamHandler(final Webcamera webcam, final Function<OutputStream, WebcamStreamSubscriber> consumerFactory) {
        _webcam = webcam;
        _consumerFactory = consumerFactory;
    }

    @Override
    public final void handle(final HttpExchange exchange) throws IOException {
        before(exchange);
        exchange.sendResponseHeaders(200, 0);

        try (OutputStream os = exchange.getResponseBody()) {
            final WebcamStreamSubscriber consumer = _consumerFactory.apply(os);
            _webcam.stream().subscribe(consumer);
            while (consumer.isSubscribed()) {
                sleep();
            }
            _webcam.stream().unsubscribe(consumer);
            exchange.close();
        } catch (Exception e) {
            _log.error("{}: request execution failed", getClass().getSimpleName(), e);
        }
    }

    protected abstract void before(final HttpExchange exchange);

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
