package hu.akarnokd.java9.rxflow;

import java.util.concurrent.Flow;

public interface QueueSubscription<T> extends SimpleQueue<T>, Flow.Subscription {

    @Override
    default boolean offer(T value) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    default boolean offer(T value1, T value2) {
        throw new UnsupportedOperationException("Should not be called");
    }

    int NONE = 0;

    int SYNC = 1;

    int ASYNC = 2;

    int BARRIER = 4;

    int requestFusion(int mode);

}
