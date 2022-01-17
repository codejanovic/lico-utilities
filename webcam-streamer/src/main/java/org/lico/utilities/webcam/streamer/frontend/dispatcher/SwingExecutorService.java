package org.lico.utilities.webcam.streamer.frontend.dispatcher;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public final class SwingExecutorService extends AbstractExecutorService {

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public void execute(final Runnable command) {
        SwingUtilities.invokeLater(command);
    }
}
