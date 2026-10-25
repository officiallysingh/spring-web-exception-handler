package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Client errors log one warn line; server errors log an error with the stack trace. */
class LoggingTest {

  private static final String STACK_FRAME = "\tat ";

  @Nested
  @SpringBootTest(classes = TestApplication.class)
  @AutoConfigureMockMvc
  @ExtendWith(OutputCaptureExtension.class)
  class DefaultLevel {

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
      assertThat(this.mvc.get().uri("/illegal-state")).hasStatus(500);
      assertThat(output.getAll())
          .contains(" ERROR ")
          .contains(
              "GET /illegal-state — 500 illegal-state-exception: IllegalStateException: db password"
                  + " is x")
          .contains("java.lang.IllegalStateException: db password is x")
          .contains(STACK_FRAME);
    }
  }

  @Nested
  @SpringBootTest(
      classes = TestApplication.class,
      properties = "logging.level.com.neolama.common.exception.advice.Exceptional=DEBUG")
  @AutoConfigureMockMvc
  @ExtendWith(OutputCaptureExtension.class)
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
