package org.lico.utilities.webcam.streamer.backend;

import akka.actor.typed.DispatcherSelector;


public class Dispatchers {

    public DispatcherSelector filesystem() {
        return DispatcherSelector.fromConfig("filesystem-dispatcher");
    }

    public DispatcherSelector webcams() {
        return DispatcherSelector.fromConfig("webcams-dispatcher");
    }

    public DispatcherSelector webserver() {
        return DispatcherSelector.fromConfig("webserver-dispatcher");
    }

    public DispatcherSelector swing() {
        return DispatcherSelector.fromConfig("swing-dispatcher");
    }
}
