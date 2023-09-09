package ru.romangr.exceptional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import ru.romangr.exceptional.nullability.NonNullApi;
import ru.romangr.exceptional.type.ExceptionalConsumer;
import ru.romangr.exceptional.type.ExceptionalFunction;
import ru.romangr.exceptional.type.ExceptionalRunnable;
import ru.romangr.exceptional.type.ExceptionalSupplier;
import ru.romangr.exceptional.type.ExceptionalWrappedException;
import ru.romangr.exceptional.type.ProcessingResult;

@NonNullApi
public final class Exceptional<T> {

  private static final Exceptional<?> EMPTY_INSTANCE = exceptional((Object) null);

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
   * Get some value from supplier catching all the exceptions.
   *
   * @param supplier to get value from.
   * @param <V>      type of the value.
   * @return an instance of {@link Exceptional} with value or exception or in empty state.
   */
  public static <V> Exceptional<V> attempt(ExceptionalSupplier<V> supplier) {
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
   * Wrap null or some value with {@link Exceptional}.
   *
   * @param value to wrap.
   * @param <V>   type of the value.
   * @return an instance of {@link Exceptional} with value or in empty state.
   */
  public static <V> Exceptional<V> of(@Nullable V value) {
    return new Exceptional<>(value);
  }

  /**
   * Wrap value from {@link Optional} with {@link Exceptional}.
   *
   * @param optional {@link Optional} to wrap value from.
   * @param <V>      type of the value.
   * @return an instance of {@link Exceptional} with value or in empty state.
   */
  public static <V> Exceptional<V> of(Optional<V> optional) {
    return new Exceptional<>(optional.orElse(null));
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
   * Wrap an exception with {@link Exceptional}.
   *
   * @param exception to wrap.
   * @param <V>       type of the exception.
   * @return an instance of {@link Exceptional} with exception.
   */
  public static <V> Exceptional<V> of(Exception exception) {
    return new Exceptional<>(exception);
  }

  /**
   * Applies mapper to every collection element until the first return {@link Exceptional} with
   * exception from mapper.
   *
   * @return an instance of {@link Exceptional} with {@link ProcessingResult}
   */
  public static <E, C> Exceptional<ProcessingResult<E>> processCollection(Collection<C> collection,
                                                                          Function<? super C, Exceptional<E>> mapper) {
    Iterator<C> iterator = collection.iterator();
    if (!iterator.hasNext()) {
      return exceptional(new ProcessingResult<>(Collections.emptyList(), null));
    }
    List<E> successResults = new ArrayList<>(collection.size());
    do {
      C element = iterator.next();
      Exceptional<E> result = mapper.apply(element);
      if (result.isValuePresent()) {
        successResults.add(result.getValue());
      }
      if (result.isException()) {
        return exceptional(new ProcessingResult<>(successResults, result.getException()));
      }
    } while (iterator.hasNext());
    return exceptional(new ProcessingResult<>(successResults, null));
  }

  @SuppressWarnings("unchecked")
  public static <E> Exceptional<E> empty() {
    return (Exceptional<E>) EMPTY_INSTANCE;
  }

  /**
   * Map the value in {@link Exceptional} to some other value. Exceptions in mapper won't be
   * caught.
   *
   * @param mapper to get a new value.
   * @param <V>    type of new value.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before mapping.
   */
  @SuppressWarnings("unchecked")
  public <V> Exceptional<V> map(Function<? super T, V> mapper) {
    if (thisIsNotValue()) {
      return (Exceptional<V>) this;
    }
    return new Exceptional<>(mapper.apply(value));
  }

  /**
   * Map the value in {@link Exceptional} to some other value catching all the exceptions from
   * mapper.
   *
   * @param mapper to get a new value.
   * @param <V>    type of new value.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before mapping or with an exception occurred in process of mapping.
   */
  @SuppressWarnings("unchecked")
  public <V> Exceptional<V> safelyMap(ExceptionalFunction<? super T, V> mapper) {
    if (thisIsNotValue()) {
      return (Exceptional<V>) this;
    }
    return getExceptional(() -> mapper.apply(this.value));
  }

  /**
   * Map the value in {@link Exceptional} to a value from another {@link Exceptional} provided by
   * mapper. Exceptions in mapper won't be caught.
   *
   * @param mapper to get a new value.
   * @param <V>    type of new value.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before mapping or with exception from the {@link Exceptional} mapper returned.
   */
  @SuppressWarnings("unchecked")
  public <V> Exceptional<V> flatMap(Function<? super T, Exceptional<V>> mapper) {
    if (this.thisIsNotValue()) {
      return (Exceptional<V>) this;
    }
    return mapper.apply(this.value);
  }

  /**
   * Map the value in {@link Exceptional} to a value from another {@link Exceptional} provided by
   * mapper if this {@link Exceptional} is in empty state. Exceptions in mapper won't be caught.
   *
   * @param supplier new {@link Exceptional} instance supplier.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before mapping or with exception from the {@link Exceptional} mapper returned.
   */
  public Exceptional<T> flatMapIfEmpty(Supplier<Exceptional<T>> supplier) {
    if (isEmpty()) {
      return supplier.get();
    }
    return this;
  }

  /**
   * Executes some logic using not null value form the {@link Exceptional}.
   *
   * @param consumer consumer of the value.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before running this method or with an exception thrown by the value consumer.
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
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before running this method or with an exception thrown by the exception consumer.
   */
  public Exceptional<T> ifException(Consumer<Exception> consumer) {
    if (this.isException()) {
      return executeSafely(() -> consumer.accept(this.exception));
    }
    return this;
  }

  /**
   * Executes some logic using not null exception form the {@link Exceptional}.
   *
   * @param consumer consumer of the exception.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before running this method or with an exception thrown by the exception consumer.
   */
  @SuppressWarnings("unchecked")
  public <E extends Exception> Exceptional<T> ifException(Class<E> clazz, Consumer<E> consumer) {
    if (this.isException() && clazz.isAssignableFrom(this.exception.getClass())) {
      return executeSafely(() -> consumer.accept((E) this.exception));
    }
    return this;
  }

  /**
   * Executes some logic if the {@link Exceptional} is in empty state.
   *
   * @param runnable to execute
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before running this method or with an exception thrown by the runnable.
   */
  public Exceptional<T> ifEmpty(ExceptionalRunnable runnable) {
    if (isEmpty()) {
      return executeSafely(runnable);
    }
    return this;
  }

  /**
   * Get the not null value from the {@link Exceptional}.
   *
   * @return the value
   * @throws IllegalStateException if the {@link Exceptional} contains exception or it's in empty
   *                               state.
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
   * @throws IllegalStateException if the {@link Exceptional} contains value or it's in empty
   *                               state.
   */
  public Exception getException() throws IllegalStateException {
    if (!this.isException()) {
      throw new IllegalStateException();
    }
    return this.exception;
  }

  /**
   * Get the value from the {@link Exceptional} or default value if the {@link Exceptional} contains
   * exception or in empty state.
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
   * Get the value from the {@link Exceptional} or default value if the {@link Exceptional} contains
   * exception or in empty state.
   *
   * @param defaultValue to return if the {@link Exceptional} contains exception or in empty state.
   * @return the value or default value.
   */
  @Nullable
  public T getOrNull() {
    if (this.thisIsNotValue()) {
      return null;
    }
    return this.value;
  }

  /**
   * Map the exception in {@link Exceptional} to some value catching all the exceptions from
   * mapper.
   *
   * @param mapper to get a new value from the exception.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * occurred in process of mapping.
   */
  public Exceptional<T> resumeOnException(ExceptionalFunction<Exception, T> mapper) {
    if (!this.isException()) {
      return this;
    }
    return getExceptional(() -> mapper.apply(this.exception));
  }

  /**
   * Map the exception in the {@link Exceptional} to some other exception. Exceptions in mapper
   * won't be caught.
   *
   * @param mapper to get a new exception
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * returned by the mapper.
   */
  public Exceptional<T> mapException(Function<Exception, Exception> mapper) {
    if (!this.isException()) {
      return this;
    }
    return exceptional(mapper.apply(this.exception));
  }

  /**
   * Map the exception in the {@link Exceptional} to some other exception. Exceptions in mapper
   * won't be caught.
   *
   * @param <E>    type of exception to map
   * @param clazz  class of exception to apply mapper to
   * @param mapper to get a new exception
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * returned by the mapper.
   */
  @SuppressWarnings("unchecked")
  public <E extends Exception> Exceptional<T> mapException(Class<E> clazz,
                                                           Function<E, Exception> mapper) {
    if (!this.isException() || !clazz.isAssignableFrom(this.exception.getClass())) {
      return this;
    }
    return exceptional(mapper.apply((E) this.exception));
  }

  /**
   * Executes some logic using not null exception from the {@link Exceptional} once per the
   * exception.
   *
   * @param consumer consumer of the exception.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before running this method or with an exception thrown by the exception consumer.
   */
  public Exceptional<T> handleException(ExceptionalConsumer<Exception> consumer) {
    if (!this.isException() || isExceptionHandled) {
      return this;
    }
    this.isExceptionHandled = true;
    return executeSafely(() -> consumer.accept(this.exception));
  }

  /**
   * Executes some logic using not null exception from the {@link Exceptional} once per the
   * exception.
   *
   * @param <E>      type of exception to handle
   * @param clazz    class of exception to handle
   * @param consumer consumer of the exception.
   * @return an instance of {@link Exceptional} with value or in empty state or with an exception
   * caught before running this method or with an exception thrown by the exception consumer.
   */
  @SuppressWarnings("unchecked")
  public <E extends Exception> Exceptional<T> handleException(Class<E> clazz,
                                                              ExceptionalConsumer<E> consumer) {
    if (!this.isException() || isExceptionHandled
        || !(clazz.isAssignableFrom(exception.getClass()))) {
      return this;
    }
    this.isExceptionHandled = true;
    return executeSafely(() -> consumer.accept((E) this.exception));
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

  /**
   * Converts {@link Exceptional} to {@link Stream}.
   *
   * @return {@link Stream} of the value if it exists, empty {@link Stream} in case of exception or
   * empty state.
   */
  public Stream<T> asStream() {
    if (thisIsNotValue()) {
      return Stream.empty();
    }
    return Stream.of(value);
  }

  /**
   * Converts {@link Exceptional} to {@link Optional}.
   *
   * @return {@link Optional} of the value if it exists, empty {@link Optional} in case of exception
   * or empty state.
   */
  public Optional<T> asOptional() {
    return Optional.ofNullable(value);
  }

  /**
   * Sometimes it's needed to integrate {@link Exceptional}-based API with APIs that expect
   * exception to be thrown. This method can be used for that. If {@link Exceptional} contains
   * exception, thrown {@link ExceptionalWrappedException} will contain this exception as a cause.
   *
   * @return value from the {@link Exceptional} if it is present.
   * @throws NullPointerException        if {@link Exceptional} is empty.
   * @throws ExceptionalWrappedException if {@link Exceptional} contains exception.
   */
  public T getOrThrow() throws ExceptionalWrappedException, NullPointerException {
    if (this.isValuePresent()) {
      return this.value;
    }
    if (this.isException()) {
      throw new ExceptionalWrappedException(this.exception);
    }
    throw new NullPointerException("Exceptional is empty");
  }

  /**
   * Sometimes it's needed to integrate {@link Exceptional}-based API with APIs that expect
   * exception to be thrown. This method can be used for that. If {@link Exceptional} contains an instance of
   * {@link RuntimeException}, it will be thrown by this method.
   *
   * @return value from the {@link Exceptional} if it is present.
   * @throws RuntimeException            if {@link Exceptional} contains an instance of {@link RuntimeException} exception.
   * @throws ExceptionalWrappedException if {@link Exceptional} contains an instance of checked exception.
   */
  @Nullable
  public T getOrThrowRuntime() throws RuntimeException, ExceptionalWrappedException, NullPointerException {
    if (this.isValuePresent()) {
      return this.value;
    }
    if (this.isException()) {
      if (this.exception instanceof RuntimeException) {
        throw (RuntimeException) this.exception;
      }
      throw new ExceptionalWrappedException(this.exception);
    }
    return null;
  }

  public boolean isEmpty() {
    return !this.isValuePresent() && !this.isException();
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

  private Exceptional<T> executeSafely(ExceptionalRunnable runnable) {
    try {
      runnable.run();
      return this;
    } catch (Exception e) {
      return new Exceptional<>(e);
    }
  }
}
