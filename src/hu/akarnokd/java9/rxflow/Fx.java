package hu.akarnokd.java9.rxflow;

import java.util.Objects;
import java.util.concurrent.Flow;

public abstract class Fx<T> implements Flow.Publisher<T> {

	public static <T> Fx<T> just(T value) {
		return new FxJust<T>(Objects.requireNonNull(value, "value is null"));
	}
}
