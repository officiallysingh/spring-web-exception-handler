package com.neolama.common.exception.advice.http;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** {@code ResponseStatusException}: its status, and its reason as detail. */
@AdviceTest
class ResponseStatusAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void detailIsTheReason() {
    assertProblem(
        this.mvc.get().uri("/http/status-reason").exchange(),
        404,
        "response-status-exception",
        "Employee 7 not found");
  }

  @Test
  void detailIsReasonPhraseWithoutReason() {
    assertProblem(
        this.mvc.get().uri("/http/status-no-reason").exchange(),
        410,
        "response-status-exception",
        "Gone");
  }

  @Test
  void unknownClientCodeFallsBackTo400() {
    assertProblem(
        this.mvc.get().uri("/http/status-499").exchange(), 400, "response-status-exception");
  }

  @Test
  void unknownServerCodeFallsBackTo500() {
    assertProblem(
        this.mvc.get().uri("/http/status-599").exchange(), 500, "response-status-exception");
  }
}
