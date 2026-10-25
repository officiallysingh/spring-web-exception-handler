package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Other multipart failures are client errors. */
@AdviceTest
class MultipartAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void multipartFailureIs400() {
    assertProblem(
        this.mvc.get().uri("/io/multipart").exchange(),
        400,
        "multipart-exception",
        "Failed to parse multipart servlet request");
  }
}
