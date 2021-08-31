package org.lico.utilities.webcamcapture.drivers.capturemanager.session;

import org.lico.utilities.webcamcapture.drivers.capturemanager.model.CaptureManagerMediaType;
import org.lico.utilities.webcamcapture.drivers.capturemanager.model.CaptureManagerSource;
import org.lico.utilities.webcamcapture.drivers.capturemanager.model.CaptureManagerStreamDescriptor;
import org.lico.utilities.webcamcapture.drivers.capturemanager.model.sinks.CaptureManagerSinkFactory;
import java.awt.*;
import java.util.List;

public interface CaptureManagerSessionControl {

    boolean init(
            final CaptureManagerSource source,
            final CaptureManagerStreamDescriptor stream,
            final CaptureManagerMediaType mediaType,
            final List<CaptureManagerSinkFactory> sinkFactories,
            Component aGraphicComponent
    );

    void start();

    void stop();
}
