package org.lico.utilities.webcam.streamer.frontend.component;

import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.Objects;

public class WebcameraMenuItem extends CustomCheckboxMenuItem<WebcameraId, WebcameraMenuItem> {


    public WebcameraMenuItem(final WebcameraId webcameraId, final boolean state) {
        super(webcameraId, String.format("/%s (%s)", webcameraId.shortName(), webcameraId.name()), state);
    }

    public WebcameraMenuItem(final WebcameraId webcameraId) {
        super(webcameraId, String.format("/%s (%s)", webcameraId.shortName(), webcameraId.name()));
    }

    @Override
    protected WebcameraMenuItem getSelf() {
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WebcameraMenuItem that = (WebcameraMenuItem) o;
        return Objects.equals(_data, that._data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_data);
    }
}
