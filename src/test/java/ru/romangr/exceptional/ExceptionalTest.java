package ru.romangr.exceptional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.romangr.exceptional.type.ExceptionalWrappedException;

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
  void resumeOnExceptionWithValue() {
    String s = "test";
    Exceptional<String> exceptional = Exceptional.exceptional(s)
        .resumeOnException(e -> "123")
        .map(v -> "test2");

    assertThat(exceptional.isValuePresent()).isTrue();
    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.getValue()).isEqualTo("test2");
  }

  @Test
  void resumeOnExceptionWhenEmpty() {
    String s = null;
    Exceptional<String> exceptional = Exceptional.exceptional(s)
        .resumeOnException(e -> "123")
        .map(v -> "test2");

    assertThat(exceptional.isValuePresent()).isFalse();
    assertThat(exceptional.isException()).isFalse();
    assertThrows(IllegalStateException.class, exceptional::getValue);
  }

  @Test
  void resumeOnExceptionWhenException() {
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
  void safelyMapWhenEmpty() {
    Exceptional<String> exceptional = Exceptional.<String>exceptional(null)
        .safelyMap(value -> "test");

    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isFalse();
  }

  @Test
  void safelyMapWhenException() {
    Exceptional<String> exceptional = Exceptional.<String>exceptional(newException())
        .safelyMap(value -> "test");

    assertThat(exceptional.isValuePresent()).isFalse();
    assertThat(exceptional.isException()).isTrue();
    assertThat(exceptional.getException()).isInstanceOf(RuntimeException.class);
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
  void flatMapIfEmptyToValue() {
    final List<Integer> integers = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    Exceptional.<Integer>exceptional(null)
        .flatMapIfEmpty(() -> Exceptional.exceptional(100))
        .ifValue(integers::add);

    assertThat(integers).hasSize(1).contains(100);
    assertThat(exceptions).hasSize(0);
  }

  @Test
  void flatMapIfEmptyToException() {
    final List<Integer> integers = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    Exceptional.<Integer>exceptional(null)
        .flatMapIfEmpty(() -> Exceptional.exceptional(newException()))
        .ifValue(integers::add)
        .ifException(exceptions::add);

    assertThat(integers).hasSize(0);
    assertThat(exceptions).hasSize(1);
  }

  @Test
  void flatMapIfEmptyToEmpty() {
    final List<Integer> integers = new ArrayList<>(1);
    final List<Exception> exceptions = new ArrayList<>(0);
    final List<String> strings = new ArrayList<>(1);
    Integer i = null;
    Exceptional.<Integer>exceptional(null)
        .flatMapIfEmpty(() -> Exceptional.exceptional(i))
        .ifValue(integers::add)
        .ifException(exceptions::add)
        .ifEmpty(() -> strings.add("test"));

    assertThat(strings).hasSize(1).contains("test");
    assertThat(integers).hasSize(0);
    assertThat(exceptions).hasSize(0);
  }

  @Test
  void flatMapIfEmptyWhenValue() {
    final List<Exception> exceptions = new ArrayList<>(0);
    final List<String> strings = new ArrayList<>(1);
    String s = null;
    Exceptional.exceptional("test")
        .flatMapIfEmpty(() -> Exceptional.exceptional("flatMapped"))
        .ifValue(strings::add)
        .ifEmpty(() -> strings.add("empty"));

    assertThat(strings).hasSize(1).contains("test");
    assertThat(exceptions).hasSize(0);
  }

  @Test
  void flatMapIfEmptyWhenException() {
    final List<Exception> exceptions = new ArrayList<>(0);
    final List<String> strings = new ArrayList<>(1);
    String s = null;
    Exceptional.<String>exceptional(newException())
        .flatMapIfEmpty(() -> Exceptional.exceptional("flatMapped"))
        .ifValue(strings::add)
        .ifEmpty(() -> strings.add("empty"))
        .ifException(e -> strings.add("test"));

    assertThat(strings).hasSize(1).contains("test");
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
  void mapExceptionWhenValue() {
    Exceptional<String> exceptional = Exceptional.exceptional("test")
        .mapException(IllegalArgumentException::new);

    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isTrue();
    assertThat(exceptional.getValue()).isEqualTo("test");
  }

  @Test
  void mapExceptionWhenEmpty() {
    Exceptional<String> exceptional = Exceptional.<String>exceptional(null)
        .mapException(IllegalArgumentException::new);

    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isFalse();
  }

  @Test
  void mapExceptionByType() {
    Exceptional<String> exceptional = Exceptional.<String>exceptional(new IllegalStateException())
        .mapException(IllegalStateException.class, IllegalArgumentException::new);

    assertThat(exceptional.getException())
        .isInstanceOf(IllegalArgumentException.class)
        .hasCauseInstanceOf(IllegalStateException.class);
  }

  @Test
  void mapExceptionByTypeWhenValue() {
    Exceptional<String> exceptional = Exceptional.exceptional("test")
        .mapException(IllegalStateException.class, IllegalArgumentException::new);

    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isTrue();
    assertThat(exceptional.getValue()).isEqualTo("test");
  }

  @Test
  void mapExceptionByTypeWhenEmpty() {
    Exceptional<String> exceptional = Exceptional.<String>exceptional(null)
        .mapException(IllegalStateException.class, IllegalArgumentException::new);

    assertThat(exceptional.isException()).isFalse();
    assertThat(exceptional.isValuePresent()).isFalse();
  }

  @Test
  void mapExceptionByTypeNotMatched() {
    Exceptional<String> exceptional = Exceptional.<String>exceptional(new IllegalStateException())
        .mapException(IllegalAccessException.class, IllegalArgumentException::new);

    assertThat(exceptional.getException())
        .isInstanceOf(IllegalStateException.class)
        .hasNoCause();
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
  void handleExceptionWithMatchedExceptionType() {
    final List<Exception> exceptions = new ArrayList<>(1);

    Exceptional.<String>exceptional(new IllegalArgumentException())
        .handleException(IllegalArgumentException.class, exceptions::add)
        .handleException(IllegalArgumentException.class, exceptions::add);

    assertThat(exceptions).hasSize(1);
  }

  @Test
  void handleExceptionWithNotMatchedExceptionType() {
    final List<Exception> exceptions = new ArrayList<>(1);

    Exceptional.<String>exceptional(new IllegalArgumentException())
        .handleException(IllegalAccessException.class, exceptions::add);

    assertThat(exceptions).isEmpty();
  }

  @Test
  void handleExceptionWithTypeWithException() {
    final List<Exception> exceptions = new ArrayList<>(1);

    Exceptional.<String>exceptional(new IllegalArgumentException())
        .handleException(IllegalArgumentException.class, e -> {
          throw new IllegalAccessException();
        })
        .handleException(IllegalAccessException.class, exceptions::add);

    assertThat(exceptions).hasSize(1);
  }

  @Test
  void handleExceptionWithTypeWithValue() {
    final List<Exception> exceptions = new ArrayList<>(1);

    Exceptional.exceptional("test")
        .handleException(IllegalAccessException.class, exceptions::add);

    assertThat(exceptions).isEmpty();
  }

  @Test
  void handleExceptionWithTypeWhenEmpty() {
    final List<Exception> exceptions = new ArrayList<>(1);

    Exceptional.<String>exceptional(null)
        .handleException(IllegalAccessException.class, exceptions::add);

    assertThat(exceptions).isEmpty();
  }

  @Test
  void handleExceptionNoException() {
    final List<Exception> exceptions = new ArrayList<>(0);

    Exceptional.exceptional("test")
        .handleException(exceptions::add)
        .handleException(exceptions::add);

    assertThat(exceptions).isEmpty();
  }

  @Test
  void asStreamValue() {
    Stream<String> stream = Exceptional.exceptional("test")
        .asStream();

    assertThat(stream).containsExactly("test");
  }

  @Test
  void asStreamEmpty() {
    Stream<String> stream = Exceptional.<String>exceptional(null)
        .asStream();

    assertThat(stream).isEmpty();
  }

  @Test
  void asStreamException() {
    Stream<String> stream = Exceptional.<String>exceptional(newException())
        .asStream();

    assertThat(stream).isEmpty();
  }


  @Test
  void asOptionalValue() {
    Optional<String> optional = Exceptional.exceptional("test")
        .asOptional();

    assertThat(optional).contains("test");
  }

  @Test
  void asOptionalEmpty() {
    Optional<String> optional = Exceptional.<String>exceptional(null)
        .asOptional();

    assertThat(optional).isEmpty();
  }

  @Test
  void asOptionalException() {
    Optional<String> optional = Exceptional.<String>exceptional(newException())
        .asOptional();

    assertThat(optional).isEmpty();
  }

  @Test
  void getOrThrowWhenValue() {
    String string = Exceptional.exceptional("test").getOrThrow();

    assertThat(string).isEqualTo("test");
  }

  @Test
  void getOrThrowWhenException() {
    assertThatThrownBy(() ->
        Exceptional.exceptional(new IllegalArgumentException("test")).getOrThrow())
        .isInstanceOf(ExceptionalWrappedException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("test");
  }

  @Test
  void getOrThrowWhenEmpty() {
    assertThatThrownBy(() -> Exceptional.exceptional(null).getOrThrow())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Exceptional is empty");
  }

  private RuntimeException newException() {
    return new RuntimeException();
  }
}
