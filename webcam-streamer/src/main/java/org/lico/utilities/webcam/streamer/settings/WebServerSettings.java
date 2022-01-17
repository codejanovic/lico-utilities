package org.lico.utilities.webcam.streamer.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WebServerSettings {
    @JsonProperty("http-port")
    public int port;

    public WebServerSettings() {
        port = 8080;
    }

    @JsonIgnore
    public boolean isValid() {
        return port > 8000;
    }
}
