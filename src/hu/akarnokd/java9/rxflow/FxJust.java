package hu.akarnokd.java9.rxflow;

import hu.akarnokd.java9.util.VH;

import java.lang.invoke.VarHandle;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;

final class FxJust<T> extends Fx<T> implements ScalarCallable<T> {

    final T value;

    FxJust(T value) {
        this.value = value;
    }

    @Override
    public T call() {
        return value;
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        s.onSubscribe(new JustSubscription<T>(s, value));
    }

    static final class JustSubscription<T>
    implements QueueSubscription<T> {
        private static final long serialVersionUID = 6168014683809033659L;

        final Subscriber<? super T> actual;

        final T value;

        int state;

        static final VarHandle STATE = VH.intField(JustSubscription.class, "state");

        public JustSubscription(Subscriber<? super T> actual, T value) {
            this.actual = actual;
            this.value = value;
        }

        @Override
        public void cancel() {
            STATE.setRelease(this, 2);
        }

        @Override
        public void request(long n) {
            if (n > 0L) {
                if ((int)STATE.getAcquire(this) == 0) {
                    STATE.setRelease(this, 1);
                    actual.onNext(value);
                    if ((int)STATE.getAcquire(this) == 1) {
                        actual.onComplete();
                    }
                }
            }
        }


        @Override
        public T poll() {
            if ((int)STATE.getAcquire(this) == 0) {
                STATE.setRelease(this, 2);
                return value;
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return (int)STATE.getAcquire(this) != 0;
        }

        @Override
        public void clear() {
            STATE.setRelease(this, 2);
        }

        @Override
        public int requestFusion(int mode) {
            return mode & SYNC;
        }
    }
}
