package com.neolama.common.exception.advice.general;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** A thrown {@code ThrowableProblem} is written as built, with request fields added. */
@AdviceTest
class ThrowableProblemAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void errorTypeProblem() {
    assertProblem(
            this.mvc.get().uri("/general/problem").exchange(),
            404,
            "order-not-found",
            "Order 42 not found")
        .extractingPath("$.title")
        .isEqualTo("Not Found");
  }

  @Test
  void parametersAreTopLevelProperties() {
    final var body =
        assertProblem(
            this.mvc.get().uri("/general/problem-with-parameters").exchange(),
            400,
            "bad-request",
            "Sample detail");
    body.extractingPath("$.orderId").isEqualTo(42);
  }

  @Test
  void severalProblemsAreListedAsErrors() {
    final var body =
        assertProblem(
            this.mvc.get().uri("/general/problems").exchange(),
            500,
            "500",
            "Internal Server Error");
    body.extractingPath("$.errors.length()").isEqualTo(2);
    body.extractingPath("$.errors[0].code").isEqualTo("a-1");
    body.extractingPath("$.errors[0].message").isEqualTo("First");
    body.extractingPath("$.errors[0].details").isEqualTo("first detail");
    body.extractingPath("$.errors[1].code").isEqualTo("b-2");
  }

  @Test
  void requestFieldsAreSet() {
    final var body =
        assertProblem(this.mvc.get().uri("/general/problem").exchange(), 404, "order-not-found");
    body.extractingPath("$.instance").isEqualTo("/general/problem");
    body.extractingPath("$.method").isEqualTo("GET");
  }
}
