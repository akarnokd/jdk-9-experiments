package hu.akarnokd.java9.queue;

import hu.akarnokd.java9.rxflow.SimpleQueue;
import hu.akarnokd.java9.util.VH;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;

public final class SpscArrayQueue<T> implements SimpleQueue<T> {

    final Object[] array;

    long producerIndex;

    long consumerIndex;

    static final VarHandle ARRAY = VH.objects();

    static final VarHandle PRODUCER_INDEX = VH.longField(SpscArrayQueue.class, "producerIndex");

    static final VarHandle CONSUMER_INDEX = VH.longField(SpscArrayQueue.class, "consumerIndex");

    public SpscArrayQueue(int capacity) {
        this.array = new Object[capacity];
    }

    public boolean offer(T value) {
        Objects.requireNonNull(value);
        Object[] a = array;
        int m = a.length - 1;
        long pi = producerIndex;

        int offset = (int)pi & m;

        if (ARRAY.getVolatile(array, offset) == null) {
            ARRAY.setRelease(array, offset, value);
            PRODUCER_INDEX.setRelease(this, pi + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean offer(T value1, T value2) {
        Objects.requireNonNull(value1);
        Objects.requireNonNull(value2);
        Object[] a = array;
        int m = a.length - 1;
        long pi = producerIndex;

        int offset = (int)(pi + 1) & m;
        if (ARRAY.getVolatile(array, offset) == null) {
            ARRAY.setRelease(array, offset, value2);
            ARRAY.setRelease(array, offset - 1, value1);
            PRODUCER_INDEX.setRelease(this, pi + 2);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public T poll() {
        Object[] a = array;
        int m = a.length - 1;
        long ci = consumerIndex;
        int offset = (int)ci & m;

        Object o = ARRAY.getVolatile(array, offset);
        if (o != null) {
            ARRAY.setRelease(array, offset, null);
            CONSUMER_INDEX.setRelease(this, ci + 1);
        }
        return (T)o;
    }

    public boolean isEmpty() {
        return (long)PRODUCER_INDEX.getVolatile(this) == (long)CONSUMER_INDEX.getVolatile(this);
    }

    public void clear() {
        while (poll() != null && !isEmpty()) ;
    }
}
