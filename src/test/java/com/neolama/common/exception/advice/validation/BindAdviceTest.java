package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** {@code BindException}: field errors first, then global errors. */
@AdviceTest
class BindAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void fieldAndGlobalErrorsBecomeViolations() {
    final var body =
        assertProblem(
            this.mvc.get().uri("/validation/bind").exchange(),
            400,
            "bind-exception",
            "Request validation failed");
    body.extractingPath("$.violations.length()").isEqualTo(2);
    body.extractingPath("$.violations[0].message").isEqualTo("must not be blank");
    body.extractingPath("$.violations[0].details").isEqualTo("Invalid value for property name");
    body.extractingPath("$.violations[1].message").isEqualTo("payload is invalid");
    body.extractingPath("$.violations[1].details")
        .isEqualTo("Invalid value for property validatedPayload");
  }
}
