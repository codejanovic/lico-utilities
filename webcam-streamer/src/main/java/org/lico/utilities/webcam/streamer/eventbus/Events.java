package org.lico.utilities.webcam.streamer.eventbus;

import org.lico.utilities.webcam.streamer.webcam.WebcamStreamSubscriber;
import org.lico.utilities.webcam.streamer.webcam.Webcamera;


public interface Events {

   interface Application {

      final class ApplicationStartedEvent {

      }
   }


   interface Server {

      final class ServerRestartEvent {

      }
   }


   interface Webcam {

      final class WebcamCloseEvent {

         public final Webcamera webcamera;

         public WebcamCloseEvent( final Webcamera webcamera ) {
            this.webcamera = webcamera;
         }
      }


      final class WebcamClosedEvent {

         public final Webcamera webcamera;

         public WebcamClosedEvent( final Webcamera webcamera ) {
            this.webcamera = webcamera;
         }
      }


      final class WebcamDisableEvent {

         public final Webcamera webcamera;

         public WebcamDisableEvent( final Webcamera webcamera ) {
            this.webcamera = webcamera;
         }
      }


      final class WebcamEnableEvent {

         public final Webcamera webcamera;

         public WebcamEnableEvent( final Webcamera webcamera ) {
            this.webcamera = webcamera;
         }
      }


      final class WebcamOpenEvent {

         public final Webcamera webcamera;

         public WebcamOpenEvent( final Webcamera webcamera ) {
            this.webcamera = webcamera;
         }
      }


      final class WebcamOpenedEvent {

         public final Webcamera webcamera;

         public WebcamOpenedEvent( final Webcamera webcamera ) {
            this.webcamera = webcamera;
         }
      }


      final class WebcamStreamSubscribeEvent {

         private final Webcamera              webcamera;
         private final WebcamStreamSubscriber subscriber;

         public WebcamStreamSubscribeEvent( final Webcamera webcamera, final WebcamStreamSubscriber subscriber ) {
            this.webcamera = webcamera;
            this.subscriber = subscriber;
         }
      }


      final class WebcamStreamUnsubscribeEvent {

         private final Webcamera              webcamera;
         private final WebcamStreamSubscriber subscriber;

         public WebcamStreamUnsubscribeEvent( final Webcamera webcamera, final WebcamStreamSubscriber subscriber ) {
            this.webcamera = webcamera;
            this.subscriber = subscriber;
         }
      }


      final class WebcamStreamUnsubscribedEvent {

         public final Webcamera              webcamera;
         public final WebcamStreamSubscriber subscriber;

         public WebcamStreamUnsubscribedEvent( final Webcamera webcamera, final WebcamStreamSubscriber subscriber ) {
            this.webcamera = webcamera;
            this.subscriber = subscriber;
         }
      }


      final class WebcamsInitiatedEvent {

      }
   }

}
