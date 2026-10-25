package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** {@code ConstraintViolationException} from AOP method validation on a {@code @Validated} bean. */
@AdviceTest
class ConstraintViolationAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void violationsUsePropertyPath() {
    final var body =
        assertProblem(
            this.mvc.get().uri("/method-validated/size").param("size", "0").exchange(),
            400,
            "constraint-violation-exception",
            "Request validation failed");
    body.extractingPath("$.violations.length()").isEqualTo(1);
    body.extractingPath("$.violations[0].code").isEqualTo("400");
    body.extractingPath("$.violations[0].message").isEqualTo("must be greater than or equal to 1");
    body.extractingPath("$.violations[0].details")
        .isEqualTo("Invalid value for property size.size");
  }
}
