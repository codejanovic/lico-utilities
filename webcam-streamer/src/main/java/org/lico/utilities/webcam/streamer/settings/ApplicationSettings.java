package org.lico.utilities.webcam.streamer.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.lico.utilities.webcam.streamer.inject.Eventbus;
import org.lico.utilities.webcam.streamer.server.WebcamHandler;
import org.lico.utilities.webcam.streamer.webcam.Webcamera;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.Subscribe;
import org.jusecase.inject.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Component
public class ApplicationSettings {
    private static final Logger _log = LogManager.getLogger(WebcamHandler.class);

    private static final ObjectMapper __mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    private static final String __userDir = System.getProperty("user.dir");
    private static final Path __settingsFilePath = Paths.get(__userDir, "config", "settings.yaml");

    @Inject
    private Eventbus _eventbus;

    private WebServerSettings server;
    private List<WebcamSettings> webcams;

    public ApplicationSettings() {
        _eventbus.register(this);
        server = new WebServerSettings();
        webcams = new ArrayList<>();
    }


    @Subscribe
    public void onWebcamOpen(final Webcamera.OpenEvent event) {
        final WebcamSettings camSettings = findOrCreate(event.webcamera);
        camSettings.setUsed(true);
        save();
    }

    @Subscribe
    public void onWebcamClose(final Webcamera.CloseEvent event) {
        final WebcamSettings camSettings = findOrCreate(event.webcamera);
        camSettings.setUsed(false);
        save();
    }


    public ApplicationSettings save() {
        try {
            final File settingsFile = new File(__settingsFilePath.toAbsolutePath().toString());
            if (!settingsFile.exists()) {
                settingsFile.getParentFile().mkdirs();
                settingsFile.createNewFile();
            }
            __mapper.writeValue(settingsFile, this);
        } catch (IOException e) {
            _log.error("unable to save settings file", e);
        }
        return this;
    }

    public ApplicationSettings load() {
        try {
            final File settingsFile = new File(__settingsFilePath.toAbsolutePath().toString());
            if (!settingsFile.exists()) {
                settingsFile.getParentFile().mkdirs();
                settingsFile.createNewFile();
            }
            final ApplicationSettings settings = __mapper.readValue(settingsFile, ApplicationSettings.class);
            server = settings.server;
            webcams = settings.webcams;
            if (!validate()) {
                server = new WebServerSettings();
                webcams = new ArrayList<>();
            }
        } catch (Exception e) {
            server = new WebServerSettings();
            webcams = new ArrayList<>();
        }
        return this;
    }

    private boolean validate() {
        return webcams.stream().map(WebcamSettings::getShortName).distinct().count() == webcams.size();
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

    public WebcamSettings findOrCreate(final Webcamera webcam) {
        return webcams.stream() //
                .filter(s -> webcam.identifier().equalsIgnoreCase(s.getIdentifier())) //
                .findFirst() //
                .orElseGet(() -> {
                    WebcamSettings newSettings = new WebcamSettings(webcam, nextAvailableCamName());
                    webcams.add(newSettings);
                    return newSettings;
                });


    }
}
