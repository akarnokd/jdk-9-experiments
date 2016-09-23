package hu.akarnokd.java9.rxflow;

import hu.akarnokd.java9.util.VH;
import org.omg.CORBA.ObjectHelper;

import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.Flow;

/**
 * Arbitrates between multiple subscriptions and re-requests unfulfilled requests from the next ones.
 */
public class SubscriptionArbiter {

    Flow.Subscription current;

    long requested;

    long missedRequested;

    static final VarHandle MISSED_REQUESTED = VH.longField(SubscriptionArbiter.class, "missedRequested");

    long missedProduced;

    static final VarHandle MISSED_PRODUCED = VH.longField(SubscriptionArbiter.class, "missedProduced");

    Flow.Subscription missedSubscription;

    static final VarHandle MISSED_SUBSCRIPTION = VH.field(SubscriptionArbiter.class, "missedSubscription", Flow.Subscription.class);

    int wip;

    static final VarHandle WIP = VH.intField(SubscriptionArbiter.class, "wip");

    private boolean tryEnter() {
        return (int)WIP.getAcquire(this) == 0 && WIP.compareAndSet(this, 0, 1);
    }

    private boolean enter() {
        return (int)WIP.getAndAdd(this, 1) == 0;
    }

    private int leave(int missed) {
        int w = (int)WIP.getAcquire(this);
        if (w == missed) {
            w = (int)WIP.getAndAdd(this, -missed);
        }
        return w;
    }

    public final void setSubscription(Flow.Subscription next) {
        Objects.requireNonNull(next, "next is null");
        if (tryEnter()) {
            Flow.Subscription curr = current;
            long r;
            if (curr == SubscriptionHelper.CANCELLED) {
                next.cancel();
                r = 0L;
            } else {
                r = requested;
                if (curr != null) {
                    curr.cancel();
                }
                current = next;
            }

            int missed = leave(1);
            if (missed != 0) {
                drainLoop(missed);
            }

            if (r != 0L) {
                next.request(r);
            }
        } else {
            SubscriptionHelper.replace(MISSED_SUBSCRIPTION, this, next);
            if (enter()) {
                drainLoop(1);
            }
        }
    }

    public final void requestMore(long n) {
        if (tryEnter()) {
            long r = requested;

            long u = addCap(r, n);

            requested = u;

            Flow.Subscription curr = current;

            int missed = leave(1);
            if (missed != 0) {
                drainLoop(missed);
            }

            if (curr != null) {
                curr.request(n);
            }

        } else {
            SubscriptionHelper.getAndAddCap(MISSED_REQUESTED, this, n);
            if (enter()) {
                drainLoop(1);
            }
        }
    }

    public final void producedMore(long n) {
        if (tryEnter()) {
            long r = requested;

            long u = r - n;

            if (u < 0L) {
                new IllegalStateException("More produced than requested: " + u).printStackTrace();
                u = 0L;
            }

            requested = u;

            int missed = leave(1);
            if (missed != 0) {
                drainLoop(missed);
            }
        } else {
            SubscriptionHelper.getAndAddCap(MISSED_PRODUCED, this, n);
            if (enter()) {
                drainLoop(1);
            }
        }
    }

    public final void cancelArbiter() {
        if (SubscriptionHelper.cancel(MISSED_SUBSCRIPTION, this)) {
            if (tryEnter()) {
                Flow.Subscription curr = current;
                current = SubscriptionHelper.CANCELLED;
                if (curr != null) {
                    curr.cancel();
                }

                int missed = leave(1);
                if (missed != 0) {
                    drainLoop(missed);
                }
            } else {
                if (enter()) {
                    drainLoop(1);
                }
            }
        }
    }

    private long addCap(long a, long b) {
        long u = a + b;
        if (u < 0L) {
            return Long.MAX_VALUE;
        }
        return u;
    }

    private void drainLoop(int missed) {
        Flow.Subscription requestFrom = null;

        long requestAmount = 0L;

        for (;;) {

            Flow.Subscription curr = current;
            boolean alreadyCancelled = curr == SubscriptionHelper.CANCELLED;

            Flow.Subscription ms;

            for (;;) {
                ms = (Flow.Subscription) MISSED_SUBSCRIPTION.getAcquire(this);
                boolean missedCancel = ms == SubscriptionHelper.CANCELLED;

                if (alreadyCancelled && !missedCancel) {
                    if (ms != null) {
                        ms.cancel();
                    }
                    break;
                } else if (!alreadyCancelled && missedCancel) {
                    if (curr != null) {
                        curr.cancel();
                    }
                    curr = SubscriptionHelper.CANCELLED;
                    current = SubscriptionHelper.CANCELLED;
                    alreadyCancelled = true;
                    break;
                }
                if (MISSED_SUBSCRIPTION.compareAndSet(ms, null)) {
                    break;
                }
            }


            if (alreadyCancelled) {

                long mr = (long) MISSED_REQUESTED.getAcquire(this);
                if (mr != 0L) {
                    mr = (long) MISSED_REQUESTED.getAndSet(this, 0L);
                }

                long r = requested;

                long u;

                long mp = 0L;

                if (r != Long.MAX_VALUE) {
                    u = addCap(r, mr);

                    if (u != Long.MAX_VALUE) {
                        mp = (long) MISSED_PRODUCED.getAcquire(this);
                        if (mp != 0L) {
                            mp = (long) MISSED_PRODUCED.getAndSet(this, 0L);
                        }

                        long v = u - mp;
                        if (v < 0) {
                            new IllegalStateException("More produced than requested: " + v).printStackTrace();
                        }
                        u = v;
                    }

                    requested = u;
                } else {
                    u = Long.MAX_VALUE;
                }

                if (ms != null) {
                    current = ms;
                    requestFrom = ms;
                    requestAmount = addCap(requestAmount, u);
                } else {
                    requestFrom = curr;
                    requestAmount = addCap(requestAmount, Math.max(0L, mr - mp));
                }
            }


            missed = leave(missed);
            if (missed == 0) {
                break;
            }
        }

        if (requestAmount != 0L) {
            requestFrom.request(requestAmount);
        }
    }
}
