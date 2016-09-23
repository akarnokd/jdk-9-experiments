package hu.akarnokd.java9.rxflow;

import hu.akarnokd.java9.queue.SpscArrayQueue;
import hu.akarnokd.java9.util.VH;

import java.lang.invoke.VarHandle;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

/**
 * Observe values on a specific Executor.
 */
final class FxObserveOn<T> extends Fx<T> {

    final Flow.Publisher<T> source;

    final Callable<ExecutorService> scheduler;

    final int prefetch;

    FxObserveOn(Flow.Publisher<T> source, Callable<ExecutorService> scheduler, int prefetch) {
        this.source = source;
        this.scheduler = scheduler;
        this.prefetch = prefetch;
    }


    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        ExecutorService executor;

        try {
            executor = scheduler.call();
        } catch (Exception ex) {
            EmptySubscription.error(ex, subscriber);
            return;
        }

        source.subscribe(new ObserveOnSubscriber<T>(subscriber, executor, prefetch, prefetch - (prefetch >> 2)));
    }

    static final class ObserveOnSubscriber<T> implements Flow.Subscriber<T>, QueueSubscription<T>, Callable<Object> {

        final Flow.Subscriber<? super T> actual;

        final ExecutorService executor;

        final int prefetch;

        final int limit;

        SimpleQueue<T> queue;

        Flow.Subscription s;

        volatile boolean done;
        Throwable error;

        volatile boolean cancelled;

        boolean outputFused;

        int inputFused;

        int wip;

        static final VarHandle WIP = VH.intField(ObserveOnSubscriber.class, "wip");

        long requested;

        static final VarHandle REQUESTED = VH.longField(ObserveOnSubscriber.class, "requested");

        long emitted;

        ObserveOnSubscriber(Flow.Subscriber<? super T> actual, ExecutorService executor, int prefetch, int limit) {
            this.actual = actual;
            this.executor = executor;
            this.prefetch = prefetch;
            this.limit = limit;
        }

        @Override
        public T poll() {
            return queue.poll();
        }

        @Override
        public boolean isEmpty() {
            return queue.isEmpty();
        }

        @Override
        public void clear() {
            queue.clear();
        }

        @Override
        public int requestFusion(int mode) {
            if ((mode & ASYNC) != 0) {
                outputFused = true;
                return ASYNC;
            }
            return NONE;
        }

        @Override
        public Object call() throws Exception {
            if (outputFused) {
                drainFused();
            } else {
                drainNormal();
            }
            return null;
        }

        void drainFused() {

        }

        void drainNormal() {
            int missed = 1;

            long e = emitted;
            long r = (long)REQUESTED.getAcquire(this);
            SimpleQueue<T> q = queue;

            for (;;) {

                while (e != r) {
                    if (cancelled) {
                        q.clear();
                        break;
                    } else {

                    }
                }

                int w = (int)WIP.getAcquire(this);
                if (w == missed) {
                    emitted = e;
                    missed = (int)WIP.getAndAdd(this, -w) - w;
                } else {
                    missed = w;
                }
            }
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            if (SubscriptionHelper.validate(this.s, subscription)) {
                this.s = subscription;

                if (subscription instanceof QueueSubscription) {
                    QueueSubscription<T> qs = (QueueSubscription<T>)subscription;

                    int m = qs.requestFusion(SYNC | ASYNC | BARRIER);

                    if (m == SYNC) {
                        inputFused = m;
                        queue = qs;
                        done = true;

                        actual.onSubscribe(this);
                        return;
                    }
                    if (m == ASYNC) {
                        inputFused = m;
                        queue = qs;

                        actual.onSubscribe(this);

                        subscription.request(prefetch);

                        return;
                    }
                }

                queue = new SpscArrayQueue<T>(prefetch);

                actual.onSubscribe(this);

                subscription.request(prefetch);
            }
        }

        @Override
        public void onNext(T item) {
            if (inputFused == NONE) {
                if (!queue.offer(item)) {
                    onError(new IllegalStateException("Upstream emitted more items than requested!"));
                    return;
                }
            }
            schedule();
        }

        @Override
        public void onError(Throwable throwable) {
            if (done) {
                throwable.printStackTrace();
                return;
            }
            error = throwable;
            done = true;
            schedule();
        }

        @Override
        public void onComplete() {
            done = true;
            schedule();
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                SubscriptionHelper.getAndAddCap(REQUESTED, this, n);
                schedule();
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
            s.cancel();
            if ((int)WIP.getAndAdd(this, 1) == 0) {
                queue.clear();
            }
        }

        void schedule() {
            if ((int)WIP.getAndAdd(this, 1) == 0) {
                executor.submit(this);
            }
        }
    }
}
