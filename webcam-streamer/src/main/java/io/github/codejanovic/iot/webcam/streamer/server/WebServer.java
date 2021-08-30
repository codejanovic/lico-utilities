package io.github.codejanovic.iot.webcam.streamer.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import io.github.codejanovic.iot.webcam.streamer.inject.Eventbus;
import io.github.codejanovic.iot.webcam.streamer.settings.ApplicationSettings;
import io.github.codejanovic.iot.webcam.streamer.webcam.WebcamStreamSubscriber;
import io.github.codejanovic.iot.webcam.streamer.webcam.Webcamera;
import io.github.codejanovic.iot.webcam.streamer.webcam.Webcameras;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.Subscribe;
import org.jusecase.inject.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WebServer {

    private final ExecutorService _httpThreadPool = Executors.newFixedThreadPool(10);
    @Inject
    private Logger _log;
    @Inject
    private Eventbus _eventbus;
    @Inject
    private Webcameras _webcams;
    @Inject
    private ApplicationSettings _settings;

    private void start() {
        try {
            _server = HttpServer.create(new InetSocketAddress(_settings.getServer().getPort()), 0);

            _server.createContext("/", (rootHandler) -> {
                byte[] response = "<!DOCTYPE html><html><body>Server is up and running</body></html>".getBytes();
                rootHandler.sendResponseHeaders(200, response.length);
                final OutputStream os = rootHandler.getResponseBody();
                os.write(response);
                os.flush();
                rootHandler.close();
            });
            for (final Webcamera webcam : _webcams.all()) {
                if (!webcam.isOpen()) {
                    continue;
                }

                final HttpContext snapshotContext = _server.createContext("/" + webcam.shortName() + "/snapshot", new WebcamSnapshotHandler(webcam, output -> new WebcamStreamSubscriber.SnapshotProducer(webcam, output)));
                _log.info("{}: exposed camera snapshot to {}", webcam.name(), snapshotContext.getPath());
                final HttpContext mjpegContext = _server.createContext("/" + webcam.shortName() + "/mjpeg", new WebcamMjpegHandler(webcam, output -> new WebcamStreamSubscriber.MjpegProducer(webcam, output)));
                _log.info("{}: exposed camera mjpeg stream to {}", webcam.name(), mjpegContext.getPath());
            }

            _server.setExecutor(_httpThreadPool);
            _server.start();

        } catch (IOException e) {
            _log.error("unable to start server", e);
        }
    }

    public WebServer() {
        _eventbus.register(this);
    }

    private HttpServer _server;

    @Subscribe
    public void onRestart(final RestartEvent event) {
        stop();
        start();
    }

    @Subscribe
    public void onWebcamClosed(final Webcamera.ClosedEvent event) {
        stop();
        start();
    }

    private void stop() {
        if (_server == null) {
            return;
        }
        try {
            _server.stop(10);
        } catch (Exception e) {
            _log.error("failed to stop webserver.", e);
        }
    }

    public static final class RestartEvent {

    }
}
