package hu.akarnokd.java9.rxflow;

import java.util.Objects;
import java.util.concurrent.Flow;

/**
 * Base class for creating and working with Flow.Publishers
 * in a fluent manner.
 *
 * @param <T> the value type
 */
public abstract class Fx<T> implements Flow.Publisher<T> {

    /**
     * Returns a Publisher that emits the specified value and completes.
     * @param value
     * @param <T>
     * @return
     */
    public static <T> Fx<T> just(T value) {
        return new FxJust<T>(Objects.requireNonNull(value, "value is null"));
    }


    /**
     * Subscribes with the given Subscriber-subclass to this Publisher and retuns
     * the subscriber itself.
     * @param subscriber
     * @param <E>
     * @return
     */
    public <E extends Flow.Subscriber<? super T>> E subscribeWith(E subscriber) {
        subscribe(subscriber);
        return subscriber;
    }

}
