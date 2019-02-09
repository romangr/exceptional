package ru.romangr.exceptional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ExceptionalUseCasesTest {

  @Test
  void parseIntPositive() {
    String possibleInt = "12345";

    Exceptional<Integer> integer
        = Exceptional.getExceptional(() -> Integer.parseInt(possibleInt));

    assertThat(integer.getValue()).isEqualTo(12345);
  }

  @Test
  void parseIntNegative() {
    String possibleInt = "123.45";

    Exceptional<Integer> integer
        = Exceptional.getExceptional(() -> Integer.parseInt(possibleInt));

    assertThat(integer.isException()).isTrue();
    integer.ifValue(value -> fail("Should be exception"))
        .ifEmpty(() -> fail("Should be exception"))
        .ifException(e -> assertThat(e).isInstanceOf(NumberFormatException.class));
  }

}
