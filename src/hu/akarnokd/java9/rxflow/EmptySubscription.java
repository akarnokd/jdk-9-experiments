package hu.akarnokd.java9.rxflow;

import java.util.concurrent.Flow;

/**
 * Represents an empty Subscription that never fires an onNext.
 */
public enum EmptySubscription implements QueueSubscription<Object> {
    INSTANCE
    ;

    @Override
    public Object poll() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void clear() {
        // no op
    }

    @Override
    public int requestFusion(int mode) {
        return mode & ASYNC;
    }

    @Override
    public void request(long n) {
        // ignored
    }

    @Override
    public void cancel() {
        // ignored
    }

    public static void error(Throwable e, Flow.Subscriber<?> s) {
        s.onSubscribe(INSTANCE);
        s.onError(e);
    }

    public static void complete(Flow.Subscriber<?> s) {
        s.onSubscribe(INSTANCE);
        s.onComplete();
    }

    public static void never(Flow.Subscriber<?> s) {
        s.onSubscribe(INSTANCE);
    }
}
