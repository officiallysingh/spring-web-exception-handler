package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** {@code ResponseStatusException} and its method-validation subclass. */
class ResponseStatusAdviceTest {

  @Nested
  @SpringBootTest(classes = TestApplication.class)
  @AutoConfigureMockMvc
  class Defaults {

    @Autowired private MockMvcTester mvc;

    @Test
    void detailIsTheReason() {
      assertThat(this.mvc.get().uri("/status-reason"))
          .hasStatus(404)
          .bodyJson()
          .extractingPath("$.detail")
          .isEqualTo("Employee 7 not found");
    }

    @Test
    void unknownClientCodeFallsBackTo400() {
      assertThat(this.mvc.get().uri("/status-499")).hasStatus(400);
    }

    @Test
    void parameterConstraintReturnsViolations() {
      assertThat(this.mvc.get().uri("/page").param("page", "-1"))
          .hasStatus(400)
          .bodyJson()
          .satisfies(
              body -> {
                body.assertThat()
                    .extractingPath("$.code")
                    .isEqualTo("handler-method-validation-exception");
                body.assertThat().extractingPath("$.detail").isEqualTo("Request validation failed");
                body.assertThat().extractingPath("$.violations.length()").isEqualTo(1);
                body.assertThat().extractingPath("$.violations[0].code").isEqualTo("400");
                body.assertThat()
                    .extractingPath("$.violations[0].message")
                    .isEqualTo("must be greater than or equal to 0");
                body.assertThat()
                    .extractingPath("$.violations[0].details")
                    .isEqualTo("Invalid value for parameter page");
              });
    }
  }

  @Nested
  @SpringBootTest(
      classes = TestApplication.class,
      properties = "spring.messages.basename=i18n/test-method-validation")
  @AutoConfigureMockMvc
  class WithProperties {

    @Autowired private MockMvcTester mvc;

    @Test
    void configuredViolationMessageWins() {
      assertThat(this.mvc.get().uri("/page").param("page", "-1"))
          .hasStatus(400)
          .bodyJson()
          .extractingPath("$.violations[0].message")
          .isEqualTo("Page must not be negative");
    }
  }
}
