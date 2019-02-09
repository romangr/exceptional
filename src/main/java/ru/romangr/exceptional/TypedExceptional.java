package ru.romangr.exceptional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TypedExceptional<T, E extends Exception> {

  private final E exception;
  private final T value;

  /**
   * @param supplier should not be null
   */
  public static <V> TypedExceptional<V, Exception> getExceptional(Supplier<V> supplier) {
    try {
      return TypedExceptional.exceptional(supplier.get());
    } catch (Exception e) {
      return TypedExceptional.exceptional(e);
    }
  }

  /**
   * @param supplier       should not be null
   * @param exceptionClass should not be null
   */
  @SuppressWarnings("unchecked")
  public static <V, E1 extends Exception> TypedExceptional<V, E1> getExceptional(Supplier<V> supplier,
      Class<E1> exceptionClass) {
    try {
      return TypedExceptional.exceptional(supplier.get());
    } catch (Exception e) {
      if (exceptionClass.isAssignableFrom(e.getClass())) {
        return TypedExceptional.exceptional((E1) e);
      }
      throw new IllegalArgumentException(e);
    }
  }

  public boolean isException() {
    return exception != null;
  }

  /**
   * @param value nullable
   */
  public static <V, E1 extends Exception> TypedExceptional<V, E1> exceptional(V value) {
    return new TypedExceptional<>(value);
  }

  /**
   * @param exception should not be null
   */
  public static <V, E1 extends Exception> TypedExceptional<V, E1> exceptional(E1 exception) {
    if (exception == null) {
      throw new IllegalArgumentException();
    }
    return new TypedExceptional<>(exception);
  }

  /**
   * @param mapper should not be null
   */
  @SuppressWarnings("unchecked")
  public <V> TypedExceptional<V, E> map(Function<T, V> mapper) {
    if (thisIsNotValue()) {
      return (TypedExceptional<V, E>) this;
    }
    return new TypedExceptional<>(mapper.apply(value));
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
  public <V> TypedExceptional<V, Exception> safelyMap(Function<T, V> mapper) {
    if (thisIsNotValue()) {
      return (TypedExceptional<V, Exception>) this;
    }
    try {
      return TypedExceptional.exceptional(mapper.apply(this.value));
    } catch (Exception e) {
      return TypedExceptional.exceptional(e);
    }
  }

  /**
   * @param consumer should not be null
   */
  public TypedExceptional<T, E> ifValue(Consumer<T> consumer) {
    if (thisIsNotValue()) {
      return this;
    }
    consumer.accept(this.value);
    return this;
  }

  /**
   * @param consumer should not be null
   */
  public TypedExceptional<T, E> ifException(Consumer<? super E> consumer) {
    if (this.isException()) {
      consumer.accept(this.exception);
    }
    return this;
  }

  /**
   * @param consumer should not be null
   */
  public TypedExceptional<T, E> ifEmpty(Runnable runnable) {
    if (!this.isValuePresent() && !this.isException()) {
      runnable.run();
    }
    return this;
  }

  public boolean isValuePresent() {
    return this.value != null;
  }

  private TypedExceptional(E exception) {
    this.exception = exception;
    this.value = null;
  }

  private TypedExceptional(T value) {
    this.value = value;
    this.exception = null;
  }

  private boolean thisIsNotValue() {
    return this.isException() || !this.isValuePresent();
  }
}
