package org.lico.utilities.webcam.streamer.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;


public class ApplicationSettings {

    private WebServerSettings server;
    private WebcameraDriver driver;
    private List<WebcamSettings> webcams;

    public ApplicationSettings() {
        server = new WebServerSettings();
        webcams = new ArrayList<>();
        driver = WebcameraDriver.Default;
    }

    public WebcamSettings findOrCreate(final WebcameraId webcam) {
        return webcams.stream() //
                .filter(s -> webcam.identifier().equalsIgnoreCase(s.getIdentifier())) //
                .findFirst() //
                .orElseGet(() -> {
                    final WebcamSettings newSettings = new WebcamSettings(webcam);
                    webcams.add(newSettings);
                    return newSettings;
                });
    }

    public WebcameraDriver getDriver() {
        return driver;
    }

    public void setDriver(final WebcameraDriver driver) {
        this.driver = driver;
    }


    @JsonIgnore
    public boolean isValid() {
        return webcams.stream().map(WebcamSettings::getShortName).distinct().count() == webcams.size()
                && server.isValid()
                && driver != null;
    }

    public WebServerSettings getServer() {
        return server;
    }

    public void setServer(final WebServerSettings server) {
        this.server = server;
    }

    public List<WebcamSettings> getWebcams() {
        return webcams;
    }

    public void setWebcams(final List<WebcamSettings> webcams) {
        this.webcams = webcams;
    }

    private String nextAvailableCamName() {
        final Set<String> shortNames = webcams.stream().map(WebcamSettings::getShortName).map(String::toLowerCase).collect(Collectors.toSet());
        return LongStream.range(0, 1000).mapToObj(i -> "CAM" + i).map(String::toLowerCase).filter(n -> !shortNames.contains(n)).findFirst().orElseThrow(IllegalStateException::new);
    }

}
