package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Size limits are reported as readable sizes; the upload detail comes from the shipped bundle. */
@AdviceTest
class SizeLimitAdviceTest {

  private static final String UPLOAD = "Upload file size exceeded the maximum allowed limit: ";

  @Autowired private MockMvcTester mvc;

  @Test
  void knownMaxUploadSize() {
    assertProblem(
        this.mvc.get().uri("/io/max-upload-size").exchange(),
        413,
        "max-upload-size-exceeded-exception",
        UPLOAD + "1 MB");
  }

  @Test
  void tomcatFileSizeLimitUsesPermittedSizeNotFieldNameDigits() {
    assertProblem(
        this.mvc.get().uri("/io/upload-file-size-tomcat").exchange(),
        413,
        "max-upload-size-exceeded-exception",
        UPLOAD + "1 MB");
  }

  @Test
  void tomcatRequestSizeLimitUsesConfiguredMaximumNotActualSize() {
    assertProblem(
        this.mvc.get().uri("/io/upload-request-size-tomcat").exchange(),
        413,
        "max-upload-size-exceeded-exception",
        UPLOAD + "1 MB");
  }

  @Test
  void unknownMaxUploadSize() {
    assertProblem(
        this.mvc.get().uri("/io/max-upload-size-unknown").exchange(),
        413,
        "max-upload-size-exceeded-exception",
        UPLOAD + "UNKNOWN");
  }

  @Test
  void dataBufferLimit() {
    assertProblem(
        this.mvc.get().uri("/io/data-buffer-limit").exchange(),
        500,
        "data-buffer-limit-exception",
        "Response data from a downstream service exceeded the allowed limit: 256 KB");
  }

  @Test
  void unknownDataBufferLimit() {
    assertProblem(
        this.mvc.get().uri("/io/data-buffer-limit-unknown").exchange(),
        500,
        "data-buffer-limit-exception",
        "Response data from a downstream service exceeded the allowed limit: UNKNOWN");
  }
}
