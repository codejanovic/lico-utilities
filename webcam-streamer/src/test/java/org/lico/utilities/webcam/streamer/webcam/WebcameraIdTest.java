package org.lico.utilities.webcam.streamer.webcam;

import io.github.java.essentials.exceptions.swallow.SwallowingCatcher;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

public class WebcameraIdTest {
    MessageDigest __digest = new SwallowingCatcher<MessageDigest>(e -> null).execute(() -> MessageDigest.getInstance("SHA-256"));

    @Test
    public void testHex() {
        final String actual = HexFormat.of().formatHex(__digest.digest("USB Video Device 0".getBytes(StandardCharsets.UTF_8)));
        final String expected = HexFormat.of().formatHex(__digest.digest("USB Video Device 0".getBytes(StandardCharsets.UTF_8)));
        assertThat(actual).isEqualTo(expected);
    }
}