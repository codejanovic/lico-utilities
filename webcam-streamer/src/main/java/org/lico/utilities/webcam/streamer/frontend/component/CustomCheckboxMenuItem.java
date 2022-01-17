package org.lico.utilities.webcam.streamer.frontend.component;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

public abstract class CustomCheckboxMenuItem<T, Self extends CustomCheckboxMenuItem<T, Self>> extends CheckboxMenuItem {

    protected final T _data;

    public CustomCheckboxMenuItem(final T data) throws HeadlessException {
        super();
        _data = data;
    }

    public CustomCheckboxMenuItem(final T data, final String label, final boolean state) throws HeadlessException {
        super(label, state);
        _data = data;
    }

    public CustomCheckboxMenuItem(final T data, final String label) throws HeadlessException {
        super(label);
        _data = data;
    }

    protected abstract Self getSelf();

    public Self whenSelected(final Consumer<T> handler) {
        addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                handler.accept(_data);
            }
        });
        return getSelf();
    }

    public Self whenUnselected(final Consumer<T> handler) {
        addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                handler.accept(_data);
            }
        });
        return getSelf();
    }

}
