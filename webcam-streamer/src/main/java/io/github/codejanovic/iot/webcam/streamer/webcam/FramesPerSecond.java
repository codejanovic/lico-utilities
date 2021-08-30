package io.github.codejanovic.iot.webcam.streamer.webcam;

import java.util.concurrent.TimeUnit;

public enum FramesPerSecond {
    FPS_10(10),
    FPS_15(15),
    FPS_30(30);

    private final int _fps;

    FramesPerSecond(final int fps) {
        _fps = fps;
    }

    public long getFrames() {
        return _fps;
    }

    public TimeUnit getTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    public long getTime() {
        return 1000/_fps;
    }
}
