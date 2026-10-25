package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Values that cannot be converted to the target type. */
@AdviceTest
class TypeMismatchAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void methodArgumentTypeMismatch() {
    assertProblem(
        this.mvc.get().uri("/validation/number").param("count", "abc").exchange(),
        400,
        "bad-request",
        "Invalid value for parameter count");
  }

  /** The test bundle also sets a non-numeric status for this key, which is ignored. */
  @Test
  void propertyTypeMismatch() {
    assertProblem(
        this.mvc.get().uri("/validation/type-mismatch").exchange(),
        400,
        "bad-request",
        "Invalid value for property count");
  }
}
