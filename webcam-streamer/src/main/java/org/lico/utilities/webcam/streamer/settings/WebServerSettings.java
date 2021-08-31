package org.lico.utilities.webcam.streamer.settings;

public class WebServerSettings {
    private int port;
    private boolean autostart;

    public WebServerSettings() {
        port = 8080;
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(final boolean autostart) {
        this.autostart = autostart;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
