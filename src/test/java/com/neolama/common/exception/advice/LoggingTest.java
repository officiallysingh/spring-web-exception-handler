package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Client errors log one warn line; server errors log an error with the stack trace. */
@AdviceTest
@ExtendWith(OutputCaptureExtension.class)
class LoggingTest {

  private static final String STACK_FRAME = "\tat ";

  @Autowired private MockMvcTester mvc;

  @Test
  void clientErrorLogsOneWarnLine(final CapturedOutput output) {
    assertThat(this.mvc.get().uri("/does-not-exist")).hasStatus(404);
    assertThat(output.getAll())
        .contains(" WARN ")
        .contains(
            "GET /does-not-exist — 404 no-resource-found-exception: NoResourceFoundException:")
        .doesNotContain(" ERROR ")
        .doesNotContain(STACK_FRAME);
  }

  @Test
  void serverErrorLogsErrorWithStackTrace(final CapturedOutput output) {
    assertThat(this.mvc.get().uri("/general/illegal-state")).hasStatus(500);
    assertThat(output.getAll())
        .contains(" ERROR ")
        .contains(
            "GET /general/illegal-state — 500 illegal-state-exception: IllegalStateException: db password is x")
        .contains("java.lang.IllegalStateException: db password is x")
        .contains(STACK_FRAME);
  }

  @Nested
  @TestPropertySource(
      properties = "logging.level.com.neolama.common.exception.advice.Exceptional=TRACE")
  class TraceLevel {

    @Autowired private MockMvcTester mvc;

    @Test
    void logsTheMessageKeysThatAreLookedUp(final CapturedOutput output) {
      assertThat(
              this.mvc
                  .post()
                  .uri("/validation/body")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"\"}"))
          .hasStatus(400);
      assertThat(output.getAll())
          .contains("Error Keys")
          .contains("MethodArgumentNotValidException.NotBlank.validatedPayload.name");
    }
  }

  @Nested
  @TestPropertySource(
      properties = "logging.level.com.neolama.common.exception.advice.Exceptional=DEBUG")
  class DebugLevel {

    @Autowired private MockMvcTester mvc;

    @Test
    void clientErrorAlsoLogsStackTraceAtDebug(final CapturedOutput output) {
      assertThat(this.mvc.get().uri("/does-not-exist")).hasStatus(404);
      assertThat(output.getAll())
          .contains(" WARN ")
          .contains(" DEBUG ")
          .contains("Stack trace for GET /does-not-exist")
          .contains(STACK_FRAME);
    }
  }
}
