package org.lico.utilities.webcam.streamer.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.Objects;

public class WebcamSettings {
    @JsonProperty("webcam-id")
    public String identifier;
    @JsonProperty("webcam-display-name")
    public String webcamName;
    @JsonProperty("webcam-short-name")
    public String shortName;
    @JsonProperty("webcam-unique-name")
    public String uniqueName;
    @JsonProperty("webcam-autostart")
    public boolean autostart;


    public WebcamSettings(final WebcameraId webcam) {
        shortName = webcam.shortName();
        webcamName = webcam.name();
        identifier = webcam.identifier();
        uniqueName = webcam.nameUnique();
        autostart = false;
    }

    public WebcamSettings() {
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
}
