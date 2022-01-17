package org.lico.utilities.webcam.streamer.webcam;

import com.github.sarxos.webcam.Webcam;
import io.github.java.essentials.exceptions.swallow.SwallowingCatcher;
import org.lico.utilities.webcam.streamer.ActorResourceName;
import org.lico.utilities.webcamcapture.drivers.capturemanager.CaptureManagerVideoDevice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public interface WebcameraId extends ActorResourceName {

    final class Unique implements WebcameraId {

        private final String _identifier;
        private final String _name;
        private final String _shortname;
        private final String _arn;

        public Unique(final Webcam webcam) {
            _identifier = HexFormat.of().formatHex(__digest.digest(determineUniqueNamePerDriver(webcam).getBytes(StandardCharsets.UTF_8)));
            _arn = "webcam-" + _identifier;
            _name = webcam.getName();
            _shortname = _identifier.substring(0, 6);
        }


        @Override
        public String arn() {
            return _arn;
        }

        private String determineUniqueNamePerDriver(final Webcam webcam) {
            if (webcam.getDevice() instanceof CaptureManagerVideoDevice) {
                return ((CaptureManagerVideoDevice) webcam.getDevice()).getId();
            }

            //if (webcam.getDevice() instanceof JavaCvDevice) {
            //return webcam.getDevice().toString();
            //}

            return webcam.getName();
        }

        @Override
        public String identifier() {
            return _identifier;
        }

        @Override
        public String name() {
            return _name;
        }

        @Override
        public String shortName() {
            return _shortname;
        }
    }

    MessageDigest __digest = new SwallowingCatcher<MessageDigest>(e -> null).execute(() -> MessageDigest.getInstance("SHA-256"));

    String identifier();

    String name();

    String shortName();
}
