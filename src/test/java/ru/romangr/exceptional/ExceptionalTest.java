package ru.romangr.exceptional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class ExceptionalTest {

  @Test
  void ofValue() {
    Exceptional<String> exceptional = Exceptional.exceptional("test");

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isTrue();
    assertThat(exceptional.getValue()).isEqualTo("test");
  }

  @Test
  void ofNullValue() {
    String s = null;
    Exceptional<String> exceptional = Exceptional.exceptional(s);

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isFalse();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void ofException() {
    Exceptional<Integer> exceptional = Exceptional.exceptional(newException());

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isTrue();
    assertThat(exceptional.isValuePresent()).isFalse();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void getException() {
    Exception exception = newException();
    Exceptional<Integer> exceptional = Exceptional.exceptional(exception);

    assertThat(exceptional.getException()).isSameAs(exception);
  }

  @Test
  void getExceptionOfValue() {
    Exceptional<String> exceptional = Exceptional.exceptional("test");

    assertThrows(IllegalStateException.class, exceptional::getException);
  }

  @Test
  void getExceptionOfNullValue() {
    String s = null;
    Exceptional<String> exceptional = Exceptional.exceptional(s);

    assertThrows(IllegalStateException.class, exceptional::getException);
  }

  @Test
  void getOrDefaultWithValue() {
    String s = "test";
    Exceptional<String> exceptional = Exceptional.exceptional(s);

    assertThat(exceptional.getOrDefault("123")).isEqualTo(s);
  }

  @Test
  void getOrDefaultWhenEmpty() {
    String s = null;
    Exceptional<String> exceptional = Exceptional.exceptional(s);

    assertThat(exceptional.getOrDefault("123")).isEqualTo("123");
  }

  @Test
  void getOrDefaultWhenException() {
    Exception exception = newException();
    Exceptional<String> exceptional = Exceptional.exceptional(exception);

    assertThat(exceptional.getOrDefault("123")).isEqualTo("123");
  }

  @Test
  void mapExceptionWithValue() {
    String s = "test";
    Exceptional<String> exceptional = Exceptional.exceptional(s)
        .resumeOnException(e -> "123")
        .map(v -> "test2");

    assertThat(exceptional.isValuePresent()).isTrue();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.getValue()).isEqualTo("test2");
  }

  @Test
  void mapExceptionWhenEmpty() {
    String s = null;
    Exceptional<String> exceptional = Exceptional.exceptional(s)
        .resumeOnException(e -> "123")
        .map(v -> "test2");

    assertThat(exceptional.isValuePresent()).isFalse();
    assertThat(exceptional.isException()).isFalse();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void mapExceptionWhenException() {
    Exception exception = newException();
    Exceptional<String> exceptional = Exceptional.exceptional(exception)
        .resumeOnException(e -> "123")
        .map(v -> v + "test2");

    assertThat(exceptional.isValuePresent()).isTrue();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.getValue()).isEqualTo("123test2");
  }

  @Test
  void ofSupplier() {
    Exceptional<String> exceptional = Exceptional.getExceptional(() -> "test");

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.getValue()).isEqualTo("test");
  }

  @Test
  void ofSupplierWithNull() {
    Exceptional<String> exceptional = Exceptional.getExceptional(() -> null);

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isFalse();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void ofSupplierWithException() {
    Exceptional<Integer> exceptional = Exceptional.getExceptional(() -> {
      throw newException();
    });

    assertThat(exceptional).isNotNull();
    assertThat(exceptional.isException()).isTrue();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void isException() {
    Exceptional<Integer> exceptional = Exceptional.exceptional(newException());

    assertThat(exceptional.isException()).isTrue();
  }

  @Test
  void isNotException() {
    Exceptional<Integer> exceptional = Exceptional.exceptional(15);

    assertThat(exceptional.isException()).isFalse();
  }

  @Test
  void map() {
    Exceptional<String> exceptional = Exceptional.exceptional(15)
        .map(value -> Integer.toString(value));

    assertThat(exceptional.getValue()).isEqualTo("15");
  }

  @Test
  void mapWhenException() {
    Exceptional<String> exceptional = Exceptional.<Integer>exceptional(newException())
        .map(value -> Integer.toString(value));

    assertThat(exceptional.isException()).isTrue();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void safelyMap() {
    Exceptional<String> exceptional = Exceptional.exceptional(15)
        .safelyMap(value -> Integer.toString(value));

    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.getValue()).isEqualTo("15");
  }

  @Test
  void safelyMapWithException() {
    Exceptional<String> exceptional = Exceptional.exceptional(15).safelyMap(value -> {
      throw newException();
    });

    assertThat(exceptional.isException()).isTrue();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void ifValue() {
    final List<String> strings = new ArrayList<>(1);
    Exceptional<String> exceptional = Exceptional.exceptional("test").ifValue(strings::add);

    assertThat(exceptional.getValue()).isEqualTo("test");
    assertThat(strings).hasSize(1).contains("test");
  }

  @Test
  void ifValueWithNull() {
    String s = null;
    final List<String> strings = new ArrayList<>(0);
    Exceptional.<String>exceptional(s).ifValue(strings::add);

    assertThat(strings).hasSize(0);
  }

  @Test
  void ifValueWithException() {
    final List<String> strings = new ArrayList<>(0);
    Exceptional.<String>exceptional(newException()).ifValue(strings::add);

    assertThat(strings).hasSize(0);
  }

  @Test
  void ifValueSafety() {
    final List<Exception> exceptions = new ArrayList<>(1);
    final List<String> strings = new ArrayList<>(1);
    Exceptional.exceptional("test")
        .ifValue(s -> {
          throw newException();
        })
        .ifException(exceptions::add)
        .ifValue(strings::add);

    assertThat(exceptions).hasSize(1);
    assertThat(strings).hasSize(0);
  }

  @Test
  void ifException() {
    final List<Exception> exceptions = new ArrayList<>(1);
    Exceptional.<String>exceptional(newException()).ifException(exceptions::add);

    assertThat(exceptions).hasSize(1);
  }

  @Test
  void ifExceptionSafety() {
    final List<Exception> exceptions = new ArrayList<>(1);
    Exceptional.<String>exceptional(new RuntimeException("Test1")).ifException(e -> {
      exceptions.add(e);
      throw new RuntimeException("Test2");
    }).ifException(exceptions::add);

    assertThat(exceptions).hasSize(2)
        .anyMatch(e -> e.getMessage().equals("Test1"))
        .anyMatch(e -> e.getMessage().equals("Test2"));
  }

  @Test
  void ifExceptionWithValue() {
    final List<Exception> exceptions = new ArrayList<>(0);
    Exceptional.exceptional("test").ifException(exceptions::add);

    assertThat(exceptions).hasSize(0);
  }

  @Test
  void ifEmpty() {
    final List<String> strings = new ArrayList<>(1);
    String s = null;
    Exceptional.exceptional(s).ifEmpty(() -> strings.add("test"));

    assertThat(strings).hasSize(1).contains("test");
  }

  @Test
  void ifEmptySafety() {
    final List<Exception> exceptions = new ArrayList<>();
    final List<String> strings = new ArrayList<>();
    String s = null;
    Exceptional.exceptional(s)
        .ifEmpty(() -> {
          throw newException();
        })
        .ifException(exceptions::add)
        .ifValue(strings::add)
        .ifEmpty(() -> strings.add("empty"));

    assertThat(exceptions).hasSize(1);
    assertThat(strings).hasSize(0);
  }

  @Test
  void conditionsCombination() {
    final List<String> strings = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    Exceptional.exceptional("test")
        .ifValue(strings::add)
        .ifException(exceptions::add)
        .ifValue(strings::add)
        .ifEmpty(strings::clear);

    assertThat(strings).hasSize(2).contains("test", "test");
    assertThat(exceptions).hasSize(0);
  }

  @Test
  void flatMapToValue() {
    final List<Integer> integers = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    Exceptional.exceptional("100")
        .flatMap(string -> Exceptional.exceptional(Integer.parseInt(string)))
        .ifValue(integers::add);

    assertThat(integers).hasSize(1).contains(100);
    assertThat(exceptions).hasSize(0);
  }

  @Test
  void flatMapToException() {
    final List<Integer> integers = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    Exceptional.exceptional("100.10")
        .flatMap(string -> Exceptional.getExceptional(() -> Integer.parseInt(string)))
        .ifValue(integers::add)
        .ifException(exceptions::add);

    assertThat(integers).hasSize(0);
    assertThat(exceptions).hasSize(1);
  }

  @Test
  void flatMapToEmpty() {
    final List<Integer> integers = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    final List<String> strings = new ArrayList<>(1);
    Integer i = null;
    Exceptional.exceptional("100.10")
        .flatMap(string -> Exceptional.exceptional(i))
        .ifValue(integers::add)
        .ifException(exceptions::add)
        .ifEmpty(() -> strings.add("test"));

    assertThat(strings).hasSize(1).contains("test");
    assertThat(integers).hasSize(0);
    assertThat(exceptions).hasSize(0);
  }

  @Test
  void flatMapEmpty() {
    final List<Integer> integers = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    final List<String> strings = new ArrayList<>(1);
    String s = null;
    Exceptional.exceptional(s)
        .flatMap(string -> Exceptional.exceptional(Integer.parseInt(string)))
        .ifValue(integers::add)
        .ifException(exceptions::add)
        .ifEmpty(() -> strings.add("test"));

    assertThat(strings).hasSize(1).contains("test");
    assertThat(integers).hasSize(0);
    assertThat(exceptions).hasSize(0);
  }

  @Test
  void flatMapException() {
    final List<Integer> integers = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    final List<String> strings = new ArrayList<>(1);
    Exceptional.<String>exceptional(newException())
        .flatMap(string -> Exceptional.exceptional(Integer.parseInt(string)))
        .ifValue(integers::add)
        .ifException(exceptions::add)
        .ifEmpty(() -> strings.add("test"));

    assertThat(exceptions).hasSize(1);
    assertThat(strings).hasSize(0);
    assertThat(integers).hasSize(0);
  }

  @Test
  void mapException() {
    Exceptional<String> exceptional = Exceptional.<String>exceptional(new IllegalStateException())
        .mapException(IllegalArgumentException::new);

    assertThat(exceptional.getException())
        .isInstanceOf(IllegalArgumentException.class)
        .hasCauseInstanceOf(IllegalStateException.class);
  }

  @Test
  void handleException() {
    final List<Exception> exceptions = new ArrayList<>(1);

    Exceptional.<String>exceptional(newException())
            .handleException(exceptions::add)
            .handleException(exceptions::add);

    assertThat(exceptions).hasSize(1);
  }

  @Test
  void handleExceptionWithException() {
    final List<Exception> exceptions = new ArrayList<>(1);

    Exceptional.<String>exceptional(newException())
            .handleException(e -> {
              throw newException();
            })
            .handleException(exceptions::add);

    assertThat(exceptions).hasSize(1);
  }

  @Test
  void handleExceptionNoException() {
    final List<Exception> exceptions = new ArrayList<>(0);

    Exceptional.exceptional("test")
            .handleException(exceptions::add)
            .handleException(exceptions::add);

    assertThat(exceptions).isEmpty();
  }

  private RuntimeException newException() {
    return new RuntimeException();
  }
}
