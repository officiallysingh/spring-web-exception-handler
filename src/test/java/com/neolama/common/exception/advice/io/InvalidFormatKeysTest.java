package com.neolama.common.exception.advice.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.neolama.common.exception.support.FormatsPayload;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;

/** Error keys derived from where a JSON value failed to parse, most specific first. */
class InvalidFormatKeysTest {

  private static final String CLASS = "com.neolama.common.exception.support.FormatsPayload";

  private final MessageNotReadableAdvice advice = new MessageNotReadableAdvice() {};

  private final JsonMapper mapper = JsonMapper.builder().build();

  private String[] keysFor(final String json) {
    final InvalidFormatException exception =
        catchThrowableOfType(
            InvalidFormatException.class, () -> this.mapper.readValue(json, FormatsPayload.class));
    return this.advice.deriveInvalidFormatExceptionErrorKeys(exception);
  }

  @Test
  void simpleField() {
    assertThat(keysFor("{\"count\":\"x\"}"))
        .containsExactly(
            "InvalidFormatException." + CLASS + ".count",
            "InvalidFormatException.FormatsPayload.count",
            "InvalidFormatException.Integer.count",
            "InvalidFormatException.count",
            "InvalidFormatException");
  }

  @Test
  void nestedObjectAddsItsClass() {
    assertThat(keysFor("{\"item\":{\"qty\":\"x\"}}"))
        .containsExactly(
            "InvalidFormatException." + CLASS + ".FormatsItem.qty",
            "InvalidFormatException.FormatsPayload.FormatsItem.qty",
            "InvalidFormatException.Integer.qty",
            "InvalidFormatException.qty",
            "InvalidFormatException");
  }

  @Test
  void listElementUsesListProperty() {
    assertThat(keysFor("{\"numbers\":[\"x\"]}"))
        .containsExactly(
            "InvalidFormatException." + CLASS + ".numbers",
            "InvalidFormatException.FormatsPayload.numbers",
            "InvalidFormatException.Integer.numbers",
            "InvalidFormatException.numbers",
            "InvalidFormatException");
  }

  @Test
  void dottedMapKeyDoesNotBreakClassNames() {
    assertThat(keysFor("{\"counts\":{\"a.b\":\"x\"}}"))
        .containsExactly(
            "InvalidFormatException." + CLASS + ".LinkedHashMap.a.b",
            "InvalidFormatException.FormatsPayload.LinkedHashMap.a.b",
            "InvalidFormatException.Integer.a.b",
            "InvalidFormatException.a.b",
            "InvalidFormatException");
  }

  @Test
  void rootValueHasOnlyPlainKey() {
    final InvalidFormatException exception =
        catchThrowableOfType(
            InvalidFormatException.class, () -> this.mapper.readValue("\"x\"", Integer.class));
    assertThat(this.advice.deriveInvalidFormatExceptionErrorKeys(exception))
        .containsExactly("InvalidFormatException");
  }
}
