package org.lico.utilities.webcam.streamer.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.ArrayList;
import java.util.List;


public class ApplicationSettings {

    @JsonProperty("http-server")
    public WebServerSettings server;
    @JsonProperty("webcam-driver")
    public WebcameraDriver driver;
    @JsonProperty("webcams")
    public List<WebcamSettings> webcams;

    public ApplicationSettings() {
        server = new WebServerSettings();
        webcams = new ArrayList<>();
        driver = WebcameraDriver.Default;
    }

    public WebcamSettings findOrCreate(final WebcameraId webcam) {
        return webcams.stream() //
                .filter(s -> webcam.identifier().equalsIgnoreCase(s.identifier)) //
                .findFirst() //
                .orElseGet(() -> {
                    final WebcamSettings newSettings = new WebcamSettings(webcam);
                    webcams.add(newSettings);
                    return newSettings;
                });
    }


    @JsonIgnore
    public boolean isValid() {
        return webcams.stream().map(ws -> ws.shortName).distinct().count() == webcams.size()
                && server.isValid()
                && driver != null;
    }


}
