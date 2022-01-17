package org.lico.utilities.webcam.streamer.frontend.dispatcher;

import akka.dispatch.DispatcherPrerequisites;
import akka.dispatch.ExecutorServiceConfigurator;
import akka.dispatch.ExecutorServiceFactory;
import com.typesafe.config.Config;

import java.util.concurrent.ThreadFactory;

public final class SwingEventThreadExecutorServiceConfigurator extends ExecutorServiceConfigurator {

    public SwingEventThreadExecutorServiceConfigurator(final Config config, final DispatcherPrerequisites prerequisites) {
        super(config, prerequisites);
    }

    @Override
    public ExecutorServiceFactory createExecutorServiceFactory(final String id, final ThreadFactory threadFactory) {
        return SwingExecutorService::new;
    }
}
