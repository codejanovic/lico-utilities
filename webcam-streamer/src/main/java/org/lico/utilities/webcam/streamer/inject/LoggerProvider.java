package org.lico.utilities.webcam.streamer.inject;

import org.apache.logging.log4j.Logger;
import org.jusecase.inject.PerClassProvider;

import static org.apache.logging.log4j.LogManager.getLogger;


public final class LoggerProvider implements PerClassProvider<Logger> {

   @Override
   public Logger get(final Class<?> aClass) {
      return getLogger(aClass);
   }
}
