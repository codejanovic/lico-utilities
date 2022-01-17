package org.lico.utilities.webcam.streamer.frontend.component;

import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;

public class DriverMenu extends Menu {

    private final Consumer<WebcameraDriver> _onDriverSelectedHandler;

    public DriverMenu(final Consumer<WebcameraDriver> onDriverSelectedHandler) throws HeadlessException {
        super("Drivers");
        _onDriverSelectedHandler = onDriverSelectedHandler;
        changeDriver(null);
    }

    public void changeDriver(final WebcameraDriver activeDriver) {
        removeAll();
        Arrays.stream(WebcameraDriver.values())
                .forEach(driver -> {
                    add(new DriverMenuItem(driver, driver.equals(activeDriver))
                            .whenSelected(_onDriverSelectedHandler));
                });
    }
}
