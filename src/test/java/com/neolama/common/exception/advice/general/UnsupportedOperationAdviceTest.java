package com.neolama.common.exception.advice.general;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * {@code UnsupportedOperationException} is 501, with a fixed detail rather than the exception
 * message.
 */
@AdviceTest
class UnsupportedOperationAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void isNotImplementedWithFixedDetail() {
    assertProblem(
        this.mvc.get().uri("/general/unsupported").exchange(),
        501,
        "unsupported-operation-exception",
        "The requested operation is not supported yet");
  }
}
