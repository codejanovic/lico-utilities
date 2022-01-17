package org.lico.utilities.webcam.streamer.webcam;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
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

    public Duration getDuration() {
        return Duration.of(getTime(), ChronoUnit.MILLIS);
    }

    public long getTime() {
        return 1000 / _fps;
    }
}
