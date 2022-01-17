package org.lico.utilities.webcam.streamer.misc;

import java.util.concurrent.TimeUnit;

public interface Expirable<T> {
    final class Expired<T> implements Expirable<T> {
        @Override
        public T get() {
            return null;
        }

        @Override
        public boolean isExpired() {
            return true;
        }
    }

    final class Value<T> implements Expirable<T> {
        private final T _value;
        private final long _durationNanos;

        public Value(final T value, final long duration, final TimeUnit unit) {
            _value = value;
            _durationNanos = System.nanoTime() + unit.toNanos(duration);
        }

        @Override
        public T get() {
            return _value;
        }

        @Override
        public boolean isExpired() {
            return System.nanoTime() > _durationNanos;
        }
    }

    T get();

    boolean isExpired();
}
