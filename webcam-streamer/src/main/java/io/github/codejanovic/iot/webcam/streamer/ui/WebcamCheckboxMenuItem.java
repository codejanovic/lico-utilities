package io.github.codejanovic.iot.webcam.streamer.ui;

import io.github.codejanovic.iot.webcam.streamer.inject.Eventbus;
import io.github.codejanovic.iot.webcam.streamer.webcam.Webcamera;
import org.jusecase.inject.Component;

import java.awt.*;
import java.awt.event.ItemEvent;

@Component
public class WebcamCheckboxMenuItem extends CheckboxMenuItem {
    private final Eventbus _eventbus;
    private final Webcamera _webcam;

    public WebcamCheckboxMenuItem(final Eventbus eventbus, final Webcamera webcam) {
        super(String.format("/%s (%s)", webcam.shortName(), webcam.name()), webcam.isOpen());
        _eventbus = eventbus;
        _webcam = webcam;

        addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                this._eventbus.send(new Webcamera.OpenEvent(_webcam));
            } else {
                this._eventbus.send(new Webcamera.CloseEvent(_webcam));
            }
        });
    }
}
