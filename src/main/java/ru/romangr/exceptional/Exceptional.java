package ru.romangr.exceptional;

import ru.romangr.exceptional.nullability.NonNullApi;
import ru.romangr.exceptional.type.ExceptionalFunction;
import ru.romangr.exceptional.type.ExceptionalSupplier;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;

@NonNullApi
public final class Exceptional<T> {

    @Nullable
    private final Exception exception;

    @Nullable
    private final T value;

    private boolean isExceptionHandled;

    /**
     * Get some value from supplier catching all the exceptions.
     *
     * @param supplier to get value from.
     * @param <V>      type of the value.
     * @return an instance of {@link Exceptional} with value or exception or in empty state.
     */
    public static <V> Exceptional<V> getExceptional(ExceptionalSupplier<V> supplier) {
        try {
            return Exceptional.exceptional(supplier.get());
        } catch (Exception e) {
            return Exceptional.exceptional(e);
        }
    }

    /**
     * Wrap null or some value with {@link Exceptional}.
     *
     * @param value to wrap.
     * @param <V>   type of the value.
     * @return an instance of {@link Exceptional} with value or in empty state.
     */
    public static <V> Exceptional<V> exceptional(@Nullable V value) {
        return new Exceptional<>(value);
    }

    /**
     * Wrap an exception with {@link Exceptional}.
     *
     * @param exception to wrap.
     * @param <V>       type of the exception.
     * @return an instance of {@link Exceptional} with exception.
     */
    public static <V> Exceptional<V> exceptional(Exception exception) {
        return new Exceptional<>(exception);
    }

    /**
     * Map the value in {@link Exceptional} to some other value. Exceptions in mapper won't be caught.
     *
     * @param mapper to get a new value.
     * @param <V>    type of new value.
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception caught before
     * mapping.
     */
    @SuppressWarnings("unchecked")
    public <V> Exceptional<V> map(Function<? super T, V> mapper) {
        if (thisIsNotValue()) {
            return (Exceptional<V>) this;
        }
        return new Exceptional<>(mapper.apply(value));
    }

    /**
     * Map the value in {@link Exceptional} to some other value catching all the exceptions from mapper.
     *
     * @param mapper to get a new value.
     * @param <V>    type of new value.
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception caught before
     * mapping or with an exception occurred in process of mapping.
     */
    @SuppressWarnings("unchecked")
    public <V> Exceptional<V> safelyMap(Function<? super T, V> mapper) {
        if (thisIsNotValue()) {
            return (Exceptional<V>) this;
        }
        return getExceptional(() -> mapper.apply(this.value));
    }

    /**
     * Map the value in {@link Exceptional} to a value from another {@link Exceptional} provided by mapper.
     * Exceptions in mapper won't be caught.
     *
     * @param mapper to get a new value.
     * @param <V>    type of new value.
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception caught before
     * mapping or with exception from the {@link Exceptional} mapper returned.
     */
    @SuppressWarnings("unchecked")
    public <V> Exceptional<V> flatMap(Function<? super T, Exceptional<V>> mapper) {
        if (this.thisIsNotValue()) {
            return (Exceptional<V>) this;
        }
        return mapper.apply(this.value);
    }

    /**
     * Executes some logic using not null value form the {@link Exceptional}.
     *
     * @param consumer consumer of the value.
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception caught before
     * running this method or with an exception thrown by the value consumer.
     */
    public Exceptional<T> ifValue(Consumer<? super T> consumer) {
        if (thisIsNotValue()) {
            return this;
        }
        return executeSafely(() -> consumer.accept(this.value));
    }

    /**
     * Executes some logic using not null exception form the {@link Exceptional}.
     *
     * @param consumer consumer of the exception.
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception caught before
     * running this method or with an exception thrown by the exception consumer.
     */
    public Exceptional<T> ifException(Consumer<Exception> consumer) {
        if (this.isException()) {
            return executeSafely(() -> consumer.accept(this.exception));
        }
        return this;
    }

    /**
     * Executes some logic if the {@link Exceptional} is in empty state.
     *
     * @param runnable to execute
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception caught before
     * running this method or with an exception thrown by the runnable.
     */
    public Exceptional<T> ifEmpty(Runnable runnable) {
        if (!this.isValuePresent() && !this.isException()) {
            return executeSafely(runnable);
        }
        return this;
    }

    /**
     * Get the not null value from the {@link Exceptional}.
     *
     * @return the value
     * @throws IllegalStateException if the {@link Exceptional} contains exception or it's in empty state.
     */
    public T getValue() throws IllegalStateException {
        if (this.isException()) {
            throw new IllegalStateException();
        }
        if (!this.isValuePresent()) {
            throw new IllegalStateException();
        }
        return this.value;
    }

    /**
     * Get the not null exception from the {@link Exceptional}.
     *
     * @return the exception
     * @throws IllegalStateException if the {@link Exceptional} contains value or it's in empty state.
     */
    public Exception getException() throws IllegalStateException {
        if (!this.isException()) {
            throw new IllegalStateException();
        }
        return this.exception;
    }

    /**
     * Get the value from the {@link Exceptional} or default value if the {@link Exceptional} contains exception or in
     * empty state.
     *
     * @param defaultValue to return if the {@link Exceptional} contains exception or in empty state.
     * @return the value or default value.
     */
    public T getOrDefault(T defaultValue) {
        if (this.thisIsNotValue()) {
            return defaultValue;
        }
        return Objects.requireNonNull(this.value);
    }

    /**
     * Map the exception in {@link Exceptional} to some value catching all the exceptions from mapper.
     *
     * @param mapper to get a new value from the exception.
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception occurred in process
     * of mapping.
     */
    public Exceptional<T> resumeOnException(ExceptionalFunction<Exception, T> mapper) {
        if (!this.isException()) {
            return this;
        }
        return getExceptional(() -> mapper.apply(this.exception));
    }

    /**
     * Map the exception in the {@link Exceptional} to some other exception. Exceptions in mapper won't be caught.
     *
     * @param mapper to get a new exception
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception returned by the
     * mapper.
     */
    public Exceptional<T> mapException(Function<Exception, Exception> mapper) {
        if (!this.isException()) {
            return this;
        }
        return exceptional(mapper.apply(this.exception));
    }

    /**
     * Executes some logic using not null exception form the {@link Exceptional} once per the exception.
     *
     * @param consumer consumer of the exception.
     * @return an instance of {@link Exceptional} with value or in empty state or with an exception caught before
     * running this method or with an exception thrown by the exception consumer.
     */
    public Exceptional<T> handleException(Consumer<Exception> consumer) {
        if (!this.isException() || isExceptionHandled) {
            return this;
        }
        this.isExceptionHandled = true;
        return executeSafely(() -> consumer.accept(this.exception));
    }

    /**
     * @return true if the {@link Exceptional} contains an exception.
     */
    public boolean isException() {
        return exception != null;
    }

    /**
     * @return true if the {@link Exceptional} contains a not null value.
     */
    public boolean isValuePresent() {
        return this.value != null;
    }

    private Exceptional(Exception exception) {
        this.exception = exception;
        this.value = null;
    }

    private Exceptional(@Nullable T value) {
        this.value = value;
        this.exception = null;
    }

    private boolean thisIsNotValue() {
        return this.isException() || !this.isValuePresent();
    }

    private Exceptional<T> executeSafely(Runnable runnable) {
        try {
            runnable.run();
            return this;
        } catch (Exception e) {
            return new Exceptional<>(e);
        }
    }
}
