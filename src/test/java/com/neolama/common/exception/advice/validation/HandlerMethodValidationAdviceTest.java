package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Constraints directly on controller parameters, validated by Spring MVC itself. */
@AdviceTest
class HandlerMethodValidationAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void eachFailedParameterIsAViolation() {
    final var body =
        assertProblem(
            this.mvc
                .get()
                .uri("/validation/page")
                .param("page", "-1")
                .param("size", "0")
                .exchange(),
            400,
            "handler-method-validation-exception",
            "Request validation failed");
    body.extractingPath("$.violations.length()").isEqualTo(2);
    body.extractingPath("$.violations[0].code").isEqualTo("400");
    body.extractingPath("$.violations[0].message").isEqualTo("Page must not be negative");
    body.extractingPath("$.violations[0].details").isEqualTo("Invalid value for parameter page");
    body.extractingPath("$.violations[1].message").isEqualTo("must be greater than or equal to 1");
    body.extractingPath("$.violations[1].details").isEqualTo("Invalid value for parameter size");
  }
}
