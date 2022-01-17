package org.lico.utilities.webcam.streamer.frontend.component;

import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;

import java.awt.*;
import java.util.Objects;

public class DriverMenuItem extends CustomCheckboxMenuItem<WebcameraDriver, DriverMenuItem> {

    public DriverMenuItem(final WebcameraDriver driver) throws HeadlessException {
        this(driver, false);
    }

    public DriverMenuItem(final WebcameraDriver driver, final boolean active) throws HeadlessException {
        super(driver, driver.name(), active);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DriverMenuItem that = (DriverMenuItem) o;
        return _data == that._data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_data);
    }

    @Override
    protected DriverMenuItem getSelf() {
        return this;
    }
}
