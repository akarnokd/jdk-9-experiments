package hu.akarnokd.java9.rxflow;

import java.lang.invoke.VarHandle;
import java.util.concurrent.Flow.Subscription;

/**
 * Help handing Subscriptions, cancellation and requesting.
 */
public enum SubscriptionHelper implements Subscription {
    CANCELLED;

    @Override
    public void cancel() {
        // ignored
    }

    @Override
    public void request(long arg0) {
        // ignored
    }

    public static boolean setOnce(VarHandle field, Object instance, Subscription s) {
        Object o = field.compareAndExchange(instance, null, s);
        if (o != null) {
            s.cancel();
            if (o != CANCELLED) {
                new RuntimeException("Subscription already set!").printStackTrace();
            }
            return false;
        }
        return true;
    }

    public static boolean deferredSetOnce(VarHandle subscription, VarHandle requested, Object instance, Subscription s) {
        if (setOnce(subscription, instance, s)) {
            long missed = (long)requested.getAndSet(instance, 0L);
            if (missed != 0L) {
                s.request(missed);
            }
            return true;
        }
        return false;
    }

    public static long getAndAddCap(VarHandle requested, Object instance, long n) {
        for (;;) {
            long r = (long)requested.getVolatile(instance);
            if (r == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            long u = r + n;
            if (u < 0L) {
                u = Long.MAX_VALUE;
            }
            if (requested.compareAndSet(instance, r, u)) {
                return r;
            }
        }
    }

    public static boolean validate(long n) {
        if (n <= 0L) {
            new IllegalArgumentException("n > 0 required but it was " + n).printStackTrace();
            return false;
        }
        return true;
    }

    public static void deferredRequested(VarHandle subscription, VarHandle requested, Object instance, long n) {
        Subscription s = (Subscription)subscription.getVolatile(instance);
        if (s != null) {
            s.request(n);
        } else {
            if (validate(n)) {
                getAndAddCap(requested, instance, n);

                s = (Subscription)subscription.getVolatile(instance);

                if (s != null) {
                    long missed = (long)requested.getAndSet(instance, 0L);
                    if (missed != 0L) {
                        s.request(missed);
                    }
                }
            }
        }
    }

    public static boolean cancel(VarHandle subscription, Object instance) {
        Subscription s = (Subscription)subscription.getVolatile(instance);
        if (s != CANCELLED) {
            s = (Subscription)subscription.getAndSet(instance, CANCELLED);
            if (s != CANCELLED) {
                if (s != null) {
                    s.cancel();
                }
                return true;
            }
        }
        return false;
    }

    public static boolean replace(VarHandle subscription, Object instance, Subscription next) {
        for (;;) {
            Subscription curr = (Subscription)subscription.getVolatile(instance);
            if (curr == CANCELLED) {
                if (next != null) {
                    next.cancel();
                }
                return false;
            }

            if (subscription.compareAndSet(instance, curr, next)) {
                return true;
            }
        }
    }

    public static boolean replaceWeak(VarHandle subscription, Object instance, Subscription next) {
        Subscription curr = (Subscription)subscription.getVolatile(instance);
        if (curr == CANCELLED) {
            if (next != null) {
                next.cancel();
            }
            return false;
        }

        Subscription witness = (Subscription)subscription.compareAndExchange(instance, curr, next);
        if (witness == CANCELLED) {
            if (next != null) {
                next.cancel();
            }
            return false;
        }
        return true;
    }
}
