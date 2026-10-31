package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;
import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Values that cannot be converted to the target type. */
@AdviceTest
@ExtendWith(OutputCaptureExtension.class)
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

  /** The detail names the request parameter, not the Java parameter it is bound to. */
  @Test
  void methodArgumentTypeMismatchNamesRequestParameter() {
    assertProblem(
        this.mvc.get().uri("/validation/renamed").param("joined_after", "yesterday").exchange(),
        400,
        "bad-request",
        "Invalid value for parameter joined_after");
  }

  /** The error key ends with the request parameter name. */
  @Test
  void methodArgumentTypeMismatchKeyUsesRequestParameter() {
    assertProblem(
        this.mvc
            .get()
            .uri("/validation/renamed-configured")
            .param("joined_before", "yesterday")
            .exchange(),
        400,
        "bad-request",
        "joined_before must be a date like 1990-12-31");
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

  /** A missing converter is a server configuration error, not a bad value from the client. */
  @Test
  void conversionNotSupportedIsServerError() {
    assertProblem(
        this.mvc.get().uri("/validation/no-converter").param("value", "abc").exchange(),
        500,
        "method-argument-conversion-not-supported-exception",
        "An unexpected error occurred");
  }

  /** The ignored status entry is reported once, as a single line without a stack trace. */
  @Test
  void invalidConfiguredStatusLogsOneWarnLine(final CapturedOutput output) {
    assertThat(this.mvc.get().uri("/validation/type-mismatch")).hasStatus(400);
    assertThat(output.getAll())
        .containsOnlyOnce("Ignoring status.TypeMismatchException=abc: not an HTTP status code")
        .doesNotContain("NumberFormatException")
        .doesNotContain("\tat ");
  }
}
