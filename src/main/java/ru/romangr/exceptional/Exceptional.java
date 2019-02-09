package ru.romangr.exceptional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Exceptional<T> {

  private final Exception exception;
  private final T value;

  /**
   * @param supplier should not be null
   */
  public static <V> Exceptional<V> getExceptional(Supplier<V> supplier) {
    try {
      return Exceptional.exceptional(supplier.get());
    } catch (Exception e) {
      return Exceptional.exceptional(e);
    }
  }

  /**
   * @param value nullable
   */
  public static <V> Exceptional<V> exceptional(V value) {
    return new Exceptional<>(value);
  }

  /**
   * @param exception should not be null
   */
  public static <V> Exceptional<V> exceptional(Exception exception) {
    if (exception == null) {
      throw new IllegalArgumentException();
    }
    return new Exceptional<>(exception);
  }

  /**
   * @param mapper should not be null
   */
  @SuppressWarnings("unchecked")
  public <V> Exceptional<V> map(Function<? super T, V> mapper) {
    if (thisIsNotValue()) {
      return (Exceptional<V>) this;
    }
    return new Exceptional<>(mapper.apply(value));
  }

  /**
   * @throws {@link IllegalStateException} in case if this is empty or contains an
   *         exception
   */
  public T getValue() {
    if (this.isException()) {
      throw new IllegalStateException();
    }
    if (!this.isValuePresent()) {
      throw new IllegalStateException();
    }
    return value;
  }

  /**
   * Maps the current value catching all the exceptions from mapper
   * 
   * @param mapper should be not null
   */
  @SuppressWarnings("unchecked")
  public <V> Exceptional<V> safelyMap(Function<? super T, V> mapper) {
    if (thisIsNotValue()) {
      return (Exceptional<V>) this;
    }
    try {
      return Exceptional.exceptional(mapper.apply(this.value));
    } catch (Exception e) {
      return Exceptional.exceptional(e);
    }
  }

  /**
   * @param mapper should be not null
   */
  @SuppressWarnings("unchecked")
  public <V> Exceptional<V> flatMap(Function<? super T, Exceptional<V>> mapper) {
    if (this.thisIsNotValue()) {
      return (Exceptional<V>) this;
    }
    return mapper.apply(this.value);
  }

  /**
   * @param consumer should not be null
   */
  public Exceptional<T> ifValue(Consumer<? super T> consumer) {
    if (thisIsNotValue()) {
      return this;
    }
    return executeSafely(() -> consumer.accept(this.value));
  }

  /**
   * @param consumer should not be null
   */
  public Exceptional<T> ifException(Consumer<Exception> consumer) {
    if (this.isException()) {
      return executeSafely(() -> consumer.accept(this.exception));
    }
    return this;
  }

  /**
   * @param consumer should not be null
   */
  public Exceptional<T> ifEmpty(Runnable runnable) {
    if (!this.isValuePresent() && !this.isException()) {
      return executeSafely(runnable);
    }
    return this;
  }

  public boolean isException() {
    return exception != null;
  }

  public boolean isValuePresent() {
    return this.value != null;
  }

  private Exceptional(Exception exception) {
    this.exception = exception;
    this.value = null;
  }

  private Exceptional(T value) {
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
