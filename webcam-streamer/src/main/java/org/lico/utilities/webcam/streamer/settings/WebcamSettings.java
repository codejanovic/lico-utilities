package org.lico.utilities.webcam.streamer.settings;

import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.Objects;

public class WebcamSettings {
    private String shortName;
    private String webcamName;

    private String identifier;
    private boolean used;


    public WebcamSettings(final WebcameraId webcam) {
        shortName = webcam.shortName();
        webcamName = webcam.name();
        identifier = webcam.identifier();
        used = false;
    }

    public WebcamSettings() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WebcamSettings that = (WebcamSettings) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
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
