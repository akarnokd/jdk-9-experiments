package hu.akarnokd.java9.rxflow;

public interface SimpleQueue<T> {

	boolean offer(T value);
	
	boolean offer(T value1, T value2);
	
	T poll();
	
	boolean isEmpty();
	
	void clear();
}
