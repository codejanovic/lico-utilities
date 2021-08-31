package org.lico.utilities.webcamcapture.drivers;

import com.github.sarxos.webcam.WebcamDevice;

public interface WebcamDeviceWithId extends WebcamDevice {
    String getId();
}
