package hu.akarnokd.java9.rxflow;

/**
 * Basic queue interface with limited features.
 * @param <T>
 */
public interface SimpleQueue<T> {

    boolean offer(T value);

    boolean offer(T value1, T value2);

    T poll();

    boolean isEmpty();

    void clear();
}
