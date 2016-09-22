package hu.akarnokd.java9.queue;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;

public final class SpscArrayQueue<T> {

	final Object[] array;
	
	long producerIndex;
	
	long consumerIndex;
	
	static final VarHandle ARRAY;
	
	static final VarHandle PRODUCER_INDEX;
	
	static final VarHandle CONSUMER_INDEX;
	
	static {
		try {
			ARRAY = MethodHandles.arrayElementVarHandle(Object[].class);
			
			PRODUCER_INDEX = MethodHandles.lookup().in(SpscArrayQueue.class)
					.findVarHandle(SpscArrayQueue.class, "producerIndex", Long.TYPE);
			
			CONSUMER_INDEX = MethodHandles.lookup().in(SpscArrayQueue.class)
					.findVarHandle(SpscArrayQueue.class, "consumerIndex", Long.TYPE);
		} catch (Exception ex) {
			throw new InternalError(ex);
		}
	}
	
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
