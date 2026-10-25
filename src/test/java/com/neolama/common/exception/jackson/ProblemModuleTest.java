package com.neolama.common.exception.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

  @Test
  void moduleIsNamed() {
    assertThat(new ProblemModule().getModuleName()).isEqualTo("ProblemModule");
  }
}
