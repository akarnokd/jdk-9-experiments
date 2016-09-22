package hu.akarnokd.java9.rxflow;

public interface QueueSubscription<T> extends SimpleQueue<T> {

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
