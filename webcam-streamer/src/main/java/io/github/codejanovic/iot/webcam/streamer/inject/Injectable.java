package io.github.codejanovic.iot.webcam.streamer.inject;

import org.jusecase.inject.Injector;

public interface Injectable {

   <T> T inject(final T entity, Class<?> clazz);

   final class InjectableInjector implements Injectable {
      private final Injector _injector;

      public InjectableInjector(final Injector injector) {
         _injector = injector;
      }

      @Override
      public <T> T inject(final T entity, final Class<?> clazz) {
         _injector.inject(entity, clazz);
         return entity;
      }
   }
}
