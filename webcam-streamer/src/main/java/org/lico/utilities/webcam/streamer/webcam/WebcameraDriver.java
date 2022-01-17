package org.lico.utilities.webcam.streamer.webcam;

import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import org.lico.utilities.webcamcapture.drivers.capturemanager.CaptureManagerDriver;


public enum WebcameraDriver {
    Default,
    //OpenCV,
    WindowsCaptureManager,
    //Vlc,
    //Ffmpeg,
    ;

    public WebcamDriver get() {
        switch (this) {
            case Default:
                return new WebcamDefaultDriver();
            case WindowsCaptureManager:
                return new CaptureManagerDriver();
            //case Ffmpeg:
            //return new FFmpegCliDriver().withPath("C:\\Users\\dammi\\Downloads\\ffmpeg-5.0-full_build\\ffmpeg-5.0-full_build\\bin\\");
            //case OpenCV:
            //return new JavaCvDriver();
            //case Vlc:
            //return new VlcjDriver();
            default:
                return new WebcamDefaultDriver();
        }
    }
}
