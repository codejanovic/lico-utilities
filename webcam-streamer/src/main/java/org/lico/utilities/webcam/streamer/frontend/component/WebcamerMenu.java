package org.lico.utilities.webcam.streamer.frontend.component;

import org.lico.utilities.webcam.streamer.webcam.WebcameraId;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class WebcamerMenu extends Menu {

    private static final class WebcameraState {
        public boolean selected;
    }

    private final MenuItem _emptyState = new MenuItem("No webcams found with the current driver");
    private final Map<WebcameraId, WebcameraState> _webcameras = new HashMap<>();
    private final Consumer<WebcameraId> _onWebcamSelectedHandler;
    private final Consumer<WebcameraId> _onWebcamUnselectedHandler;

    public WebcamerMenu(final Consumer<WebcameraId> onWebcamSelectedHandler, final Consumer<WebcameraId> onWebcamUnselectedHandler) throws HeadlessException {
        super("Webcams");
        _onWebcamSelectedHandler = onWebcamSelectedHandler;
        _onWebcamUnselectedHandler = onWebcamUnselectedHandler;
        add(_emptyState);
    }

    private void rebuildUI() {
        removeAll();
        if (_webcameras.isEmpty()) {
            add(_emptyState);
            return;
        }
        _webcameras.forEach((id, state) -> add(new WebcameraMenuItem(id, state.selected)
                .whenSelected(_onWebcamSelectedHandler)
                .whenSelected(selectedId -> _webcameras.get(selectedId).selected = true)
                .whenUnselected(_onWebcamUnselectedHandler)
                .whenUnselected(unselectedId -> _webcameras.get(unselectedId).selected = false)
        ));
    }

    public void addWebcam(final WebcameraId webcameraId) {
        _webcameras.put(webcameraId, new WebcameraState());
        rebuildUI();
    }

    public void openWebcam(final WebcameraId webcameraId) {
        _webcameras.get(webcameraId).selected = true;
        rebuildUI();
    }

    public void removeWebcam(final WebcameraId webcameraId) {
        _webcameras.remove(webcameraId);
        rebuildUI();
    }

    public void closeWebcam(final WebcameraId webcameraId) {
        _webcameras.get(webcameraId).selected = false;
        rebuildUI();
    }
}
