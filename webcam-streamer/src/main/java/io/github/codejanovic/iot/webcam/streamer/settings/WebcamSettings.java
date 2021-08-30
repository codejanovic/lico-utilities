package io.github.codejanovic.iot.webcam.streamer.settings;

import io.github.codejanovic.iot.webcam.streamer.webcam.Webcamera;

import java.util.Objects;

public class WebcamSettings {
    private String shortName;
    private String webcamName;

    private String identifier;

    public WebcamSettings(final Webcamera webcam, final String shortName) {
        this.shortName = shortName;
        webcamName = webcam.name();
        identifier = webcam.identifier();
        used = false;
    }

    public String getIdentifier() {
        return identifier;
    }

    private boolean used;

    public WebcamSettings() {
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WebcamSettings that = (WebcamSettings) o;
        return Objects.equals(webcamName, that.webcamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webcamName);
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(final String shortName) {
        this.shortName = shortName;
    }

    public String getWebcamName() {
        return webcamName;
    }

    public void setWebcamName(final String webcamName) {
        this.webcamName = webcamName;
    }

    public boolean isUsed() {
        return this.used;
    }

    public void setUsed(final boolean used) {
        this.used = used;
    }
}
