package ru.romangr.exceptional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

@Tag("unit")
class TypedExceptionalTest {

  @Test
  void ofValue() {
    TypedExceptional<String, Exception> exceptional
        = TypedExceptional.exceptional("test");

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isTrue();
    assertThat(exceptional.getValue()).isEqualTo("test");
  }

  @Test
  void ofNullValue() {
    String s = null;
    TypedExceptional<String, Exception> exceptional = TypedExceptional.exceptional(s);

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isFalse();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void ofException() {
    TypedExceptional<Integer, RuntimeException> exceptional
        = TypedExceptional.exceptional(newException());

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isTrue();
    assertThat(exceptional.isValuePresent()).isFalse();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void ofSupplier() {
    TypedExceptional<String, Exception> exceptional
        = TypedExceptional.getExceptional(() -> "test");

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.getValue()).isEqualTo("test");
  }

  @Test
  void ofSupplierWithNull() {
    TypedExceptional<String, Exception> exceptional = TypedExceptional.getExceptional(() -> null);

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isFalse();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void ofSupplierWithException() {
    TypedExceptional<Integer, IllegalStateException> exceptional
        = TypedExceptional.getExceptional(
        () -> {
          throw new IllegalStateException();
        },
        IllegalStateException.class
    );

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isTrue();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void ofSupplierWithWrongException() {
    Executable executable = () -> TypedExceptional.getExceptional(
        () -> {
          throw new RuntimeException();
        },
        IllegalStateException.class
    );
    assertThrows(IllegalArgumentException.class, executable);
  }

  @Test
  void isException() {
    TypedExceptional<Integer, Exception> exceptional
        = TypedExceptional.exceptional(newException());

    assertThat(exceptional.isException()).isTrue();
  }

  @Test
  void isNotException() {
    TypedExceptional<Integer, Exception> exceptional = TypedExceptional.exceptional(15);

    assertThat(exceptional.isException()).isFalse();
  }

  @Test
  void map() {
    TypedExceptional<String, Exception> exceptional = TypedExceptional.exceptional(15)
        .map(value -> Integer.toString(value));

    assertThat(exceptional.getValue()).isEqualTo("15");
  }

  @Test
  void mapException() {
    TypedExceptional<String, Exception> exceptional
        = TypedExceptional.<Integer, Exception>exceptional(newException())
        .map(value -> Integer.toString(value));

    assertThat(exceptional.isException()).isTrue();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void safelyMap() {
    TypedExceptional<String, Exception> exceptional = TypedExceptional.exceptional(15)
        .safelyMap(value -> Integer.toString(value));

    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.getValue()).isEqualTo("15");
  }

  @Test
  void safelyMapWithException() {
    TypedExceptional<String, Exception> exceptional = TypedExceptional.exceptional(15)
        .safelyMap(value -> {
          throw newException();
        });

    assertThat(exceptional.isException()).isTrue();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void ifValue() {
    final List<String> strings = new ArrayList<>(1);
    TypedExceptional<String, Exception> exceptional = TypedExceptional.exceptional("test")
        .ifValue(strings::add);

    assertThat(exceptional.getValue()).isEqualTo("test");
    assertThat(strings).hasSize(1).contains("test");
  }

  @Test
  void ifValueWithNull() {
    final List<String> strings = new ArrayList<>(0);
    String s = null;
    TypedExceptional.<String, Exception>exceptional(s)
        .ifValue(strings::add);

    assertThat(strings).hasSize(0);
  }

  @Test
  void ifValueWithException() {
    final List<String> strings = new ArrayList<>(0);
    TypedExceptional.<String, RuntimeException>exceptional(newException())
        .ifValue(strings::add);

    assertThat(strings).hasSize(0);
  }

  @Test
  void ifException() {
    final List<RuntimeException> exceptions = new ArrayList<>(1);
    TypedExceptional.<String, RuntimeException>exceptional(newException())
        .ifException(exceptions::add);

    assertThat(exceptions).hasSize(1);
  }

  @Test
  void ifExceptionWithValue() {
    final List<Exception> exceptions = new ArrayList<>(0);
    TypedExceptional.exceptional("test")
        .ifException(exceptions::add);

    assertThat(exceptions).hasSize(0);
  }

  @Test
  void ifEmpty() {
    final List<String> strings = new ArrayList<>(1);
    String s = null;
    TypedExceptional.<String, RuntimeException>exceptional(s)
        .ifEmpty(() -> strings.add("test"));

    assertThat(strings).hasSize(1).contains("test");
  }

  @Test
  void conditionsCombination() {
    final List<String> strings = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    TypedExceptional.exceptional("test")
        .ifValue(strings::add)
        .ifException(exceptions::add)
        .ifValue(strings::add)
        .ifEmpty(strings::clear);

    assertThat(strings).hasSize(2).contains("test", "test");
    assertThat(exceptions).hasSize(0);
  }

  private RuntimeException newException() {
    return new RuntimeException();
  }
}
