package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Unreadable request bodies: malformed JSON, and values of the wrong format. */
@AdviceTest
class MessageNotReadableAdviceTest {

  @Autowired private MockMvcTester mvc;

  private MvcTestResult post(final String json) {
    return this.mvc
        .post()
        .uri("/io/formats")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .exchange();
  }

  @Test
  void malformedJsonHidesParserMessage() {
    assertProblem(
        post("{not json"), 400, "http-message-not-readable-exception", "Malformed request body");
  }

  @Test
  void invalidFormatUsesMostSpecificConfiguredKey() {
    assertProblem(post("{\"count\":\"x\"}"), 400, "bad-request", "Count must be a whole number");
  }

  @Test
  void invalidFormatFallsBackToPlainKey() {
    assertProblem(
        post("{\"item\":{\"qty\":\"x\"}}"), 400, "bad-request", "Some value has the wrong format");
  }

  @Test
  void dottedMapKeyDoesNotCrashTheHandler() {
    assertProblem(
        post("{\"counts\":{\"a.b\":\"x\"}}"),
        400,
        "bad-request",
        "Some value has the wrong format");
  }

  @Test
  void listElementUsesConfiguredCode() {
    assertProblem(
        post("{\"numbers\":[\"x\"]}"),
        400,
        "numbers-not-a-number",
        "Some value has the wrong format");
  }
}
