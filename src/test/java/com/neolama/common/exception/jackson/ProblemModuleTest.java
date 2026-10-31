package com.neolama.common.exception.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

/** {@link ProblemModule} writes status codes as numbers and methods as names, and reads them. */
class ProblemModuleTest {

  private final JsonMapper mapper = JsonMapper.builder().addModule(new ProblemModule()).build();

  @Test
  void knownStatusRoundTrips() {
    assertThat(this.mapper.writeValueAsString(HttpStatus.NOT_FOUND)).isEqualTo("404");
    assertThat(this.mapper.readValue("404", HttpStatusCode.class)).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void unknownStatusRoundTripsInsteadOfNull() {
    final HttpStatusCode status = this.mapper.readValue("499", HttpStatusCode.class);
    assertThat(status).isNotNull();
    assertThat(status.value()).isEqualTo(499);
    assertThat(this.mapper.writeValueAsString(status)).isEqualTo("499");
  }

  @Test
  void methodRoundTripsAsName() {
    assertThat(this.mapper.writeValueAsString(HttpMethod.GET)).isEqualTo("\"GET\"");
    assertThat(this.mapper.readValue("\"GET\"", HttpMethod.class)).isEqualTo(HttpMethod.GET);
  }

  @Test
  void nullValuesStayNull() {
    assertThat(this.mapper.readValue("null", HttpMethod.class)).isNull();
    assertThat(this.mapper.readValue("null", HttpStatusCode.class)).isNull();
  }

  /** Jackson writes {@code null} itself; the serializers are never called with it. */
  @Test
  void nullValuesInContainersAreWrittenAsNull() {
    final List<@Nullable HttpMethod> methods = new ArrayList<>();
    methods.add(null);
    final Map<String, @Nullable HttpStatusCode> statuses = new HashMap<>();
    statuses.put("status", null);
    assertThat(this.mapper.writeValueAsString(new Holder(null, null)))
        .isEqualTo("{\"method\":null,\"status\":null}");
    assertThat(this.mapper.writeValueAsString(methods)).isEqualTo("[null]");
    assertThat(this.mapper.writeValueAsString(statuses)).isEqualTo("{\"status\":null}");
  }

  @Test
  void methodMustBeANonBlankString() {
    for (final String json : new String[] {"123", "true", "\"\"", "\"  \"", "{}", "[]"}) {
      assertThatExceptionOfType(MismatchedInputException.class)
          .as(json)
          .isThrownBy(() -> this.mapper.readValue(json, HttpMethod.class))
          .withMessageContaining("HTTP method must be a non-blank JSON string");
    }
  }

  @Test
  void statusMustBeAThreeDigitInteger() {
    for (final String json : new String[] {"1000", "99", "\"404\"", "4.04", "{}"}) {
      assertThatExceptionOfType(MismatchedInputException.class)
          .as(json)
          .isThrownBy(() -> this.mapper.readValue(json, HttpStatusCode.class))
          .withMessageContaining("HTTP status must be an integer from 100 to 999");
    }
  }

  @Test
  void moduleIsNamed() {
    assertThat(new ProblemModule().getModuleName()).isEqualTo("ProblemModule");
  }

  /** A bean with nullable method and status, for serialization. */
  record Holder(@Nullable HttpMethod method, @Nullable HttpStatusCode status) {}
}
