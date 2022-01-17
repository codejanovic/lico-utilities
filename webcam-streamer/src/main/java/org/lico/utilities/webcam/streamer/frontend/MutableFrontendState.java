package org.lico.utilities.webcam.streamer.frontend;

import org.lico.utilities.webcam.streamer.webcam.WebcameraDriver;
import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MutableFrontendState {

    public final List<WebcameraId> webcameras = new ArrayList<>();
    public final Set<WebcameraId> webcamerasActive = new HashSet<>();
    public WebcameraDriver driverActive = null;
}
