package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** {@code @Valid} request bodies: one violation per failed field, with flat message arguments. */
@AdviceTest
class MethodArgumentNotValidAdviceTest {

  @Autowired private MockMvcTester mvc;

  private MvcTestResult post(final String json) {
    return this.mvc
        .post()
        .uri("/validation/body")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .exchange();
  }

  @Test
  void defaultViolation() {
    final var body =
        assertProblem(
            post("{\"name\":\"\"}"),
            400,
            "method-argument-not-valid-exception",
            "Request validation failed");
    body.extractingPath("$.violations.length()").isEqualTo(1);
    body.extractingPath("$.violations[0].code").isEqualTo("400");
    body.extractingPath("$.violations[0].message").isEqualTo("must not be blank");
    body.extractingPath("$.violations[0].details").isEqualTo("Invalid value for property name");
  }

  @Test
  void configuredMessageGetsFieldNameAndConstraintAttributes() {
    final var body =
        assertProblem(
            post("{\"name\":\"x\",\"nick\":\"toolongnick\"}"),
            400,
            "method-argument-not-valid-exception");
    body.extractingPath("$.violations[0].message")
        .isEqualTo("Nick nick must be 2 to 5 characters, got 'toolongnick'");
  }

  @Test
  void everyFailedFieldIsListed() {
    assertProblem(
            post("{\"name\":\"\",\"nick\":\"toolongnick\"}"),
            400,
            "method-argument-not-valid-exception")
        .extractingPath("$.violations.length()")
        .isEqualTo(2);
  }
}
