package hu.akarnokd.java9.rxflow;

import java.util.concurrent.Callable;

public interface ScalarCallable<T> extends Callable<T> {

	@Override
	T call();
}
