package hu.akarnokd.java9.rxflow;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.atomic.AtomicInteger;

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
	extends AtomicInteger
	implements Flow.Subscription {
		private static final long serialVersionUID = 6168014683809033659L;

		final Subscriber<? super T> actual;
		
	    final T value;
	    
		public JustSubscription(Subscriber<? super T> actual, T value) {
			this.actual = actual;
			this.value = value;
		}

		@Override
		public void cancel() {
			lazySet(2);
		}

		@Override
		public void request(long n) {
			if (n > 0L) {
				if (compareAndSet(0, 1)) {
					actual.onNext(value);
					if (get() == 1) {
						actual.onComplete();
					}
				}
			}
		}
		
	}
}
