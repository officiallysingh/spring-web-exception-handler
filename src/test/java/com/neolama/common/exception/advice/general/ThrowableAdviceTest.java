package com.neolama.common.exception.advice.general;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Fallback for any exception without a dedicated advice. The status comes from {@code
 * status.{SimpleName}}, then {@code @ResponseStatus}, then a Spring {@code ErrorResponse}, then
 * 500.
 */
@AdviceTest
class ThrowableAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void unknownExceptionIs500AndHidesItsMessage() {
    assertProblem(
        this.mvc.get().uri("/general/illegal-state").exchange(),
        500,
        "illegal-state-exception",
        "An unexpected error occurred");
  }

  @Test
  void responseStatusAnnotationSetsStatusAndKeepsClientMessage() {
    assertProblem(
        this.mvc.get().uri("/general/conflict").exchange(),
        409,
        "conflict-exception",
        "Order 7 already exists");
  }

  @Test
  void responseStatusAnnotationOnCauseIsFound() {
    assertProblem(
        this.mvc.get().uri("/general/wrapped-conflict").exchange(), 409, "illegal-state-exception");
  }

  @Test
  void errorResponseExceptionUsesItsOwnStatus() {
    assertProblem(
        this.mvc.get().uri("/general/async-timeout").exchange(),
        503,
        "async-request-timeout-exception");
  }

  @Test
  void configuredStatusIsUsed() {
    assertProblem(
        this.mvc.get().uri("/general/configured-status").exchange(),
        418,
        "configured-status-exception");
  }

  @Test
  void invalidConfiguredStatusFallsBack() {
    assertProblem(
        this.mvc.get().uri("/general/invalid-status").exchange(), 500, "invalid-status-exception");
  }

  @Nested
  @TestPropertySource(
      properties = "spring.messages.basename=i18n/test-overrides," + AdviceTest.BASENAMES)
  class WithStatusOverride {

    @Autowired private MockMvcTester mvc;

    @Test
    void configuredStatusWinsOverErrorResponseStatus() {
      assertProblem(
          this.mvc.get().uri("/does-not-exist").exchange(), 410, "no-resource-found-exception");
    }
  }
}
