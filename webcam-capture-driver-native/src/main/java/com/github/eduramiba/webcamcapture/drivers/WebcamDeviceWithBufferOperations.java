package com.github.eduramiba.webcamcapture.drivers;

import com.github.sarxos.webcam.WebcamDevice;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public interface WebcamDeviceWithBufferOperations extends WebcamDevice {

    BufferedImage getImage(final ByteBuffer byteBuffer);

}
