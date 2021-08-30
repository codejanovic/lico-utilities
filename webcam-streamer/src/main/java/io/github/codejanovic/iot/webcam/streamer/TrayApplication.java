package io.github.codejanovic.iot.webcam.streamer;

import io.github.codejanovic.iot.webcam.streamer.inject.Eventbus;
import io.github.codejanovic.iot.webcam.streamer.server.WebServer;
import io.github.codejanovic.iot.webcam.streamer.ui.WebcamCheckboxMenuItem;
import io.github.codejanovic.iot.webcam.streamer.ui.WebcamSnapshotMenuItem;
import io.github.codejanovic.iot.webcam.streamer.ui.WebcamStreamMenuItem;
import io.github.codejanovic.iot.webcam.streamer.webcam.Webcamera;
import io.github.codejanovic.iot.webcam.streamer.webcam.Webcameras;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jusecase.inject.Component;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

@Component
public class TrayApplication {

    @Inject
    private Eventbus _eventbus;
    @Inject
    private Logger _log;
    @Inject
    private Webcameras _webcams;
    private final TrayIcon _trayIcon;

    public TrayApplication() throws Exception {
        _eventbus.register(this);
        final BufferedImage trayIconImage = ImageIO.read(
                new URL("https://cdn4.iconfinder.com/data/icons/social-messaging-ui-color-squares-01/3/72-512.png"));
        _trayIcon = new TrayIcon(trayIconImage.getScaledInstance(SystemTray.getSystemTray().getTrayIconSize().width, -1, Image.SCALE_SMOOTH));
        rebuild();
        SystemTray.getSystemTray().add(_trayIcon);
        _eventbus.send(new Bootstrap.ApplicationStartedEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebcamsInitiated(final Webcameras.WebcamsInitiatedEvent event) {
        SwingUtilities.invokeLater(this::rebuild);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebcameraClosedEvent(final Webcamera.ClosedEvent event) {
        SwingUtilities.invokeLater(this::rebuild);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWebcameraOpenedEvent(final Webcamera.OpenedEvent event) {
        SwingUtilities.invokeLater(this::rebuild);
    }


    private void rebuild() {
        try {
            _trayIcon.setPopupMenu(null);
            final PopupMenu popup = new PopupMenu();

            final MenuItem menuExit = new MenuItem("Exit");
            menuExit.addActionListener(ae -> {
                System.exit(0);
            });

            final MenuItem menuRestartServer = new MenuItem("Restart server");
            menuRestartServer.addActionListener(l -> _eventbus.send(new WebServer.RestartEvent()));

            popup.add(menuRestartServer);
            popup.addSeparator();
            if (_webcams.all().isEmpty()) {
                popup.add(new MenuItem("No Webcams detected"));
                popup.addSeparator();
            } else {
                for (Webcamera webcam : _webcams.all()) {
                    popup.add(new WebcamCheckboxMenuItem(_eventbus, webcam));
                    if (webcam.isOpen()) {
                        popup.add(new WebcamStreamMenuItem(webcam));
                        popup.add(new WebcamSnapshotMenuItem(webcam));
                    }
                    popup.addSeparator();
                }
            }

            popup.add(menuExit);

            _trayIcon.setPopupMenu(popup);

        } catch (Exception e) {
            _log.error("error upon application startup", e);
        }
    }


}
