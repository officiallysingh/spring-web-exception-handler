package com.neolama.common.exception.advice.http;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;
import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** 415 and 406 responses carry the supported media types in {@code Accept}. */
@AdviceTest
class MediaTypeAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void unsupportedContentType() {
    final MvcTestResult result =
        this.mvc
            .post()
            .uri("/http/json-body")
            .contentType(MediaType.TEXT_PLAIN)
            .content("x")
            .exchange();
    assertProblem(
        result,
        415,
        "http-media-type-not-supported-exception",
        "Media Type: text/plain Not Supported, Supported Media Types are: application/json");
    assertThat(result).headers().hasValue(HttpHeaders.ACCEPT, "application/json");
  }

  @Test
  void unsupportedMediaTypeStatusException() {
    final MvcTestResult result =
        this.mvc.get().uri("/http/webflux-unsupported-media-type").exchange();
    assertProblem(
        result,
        415,
        "unsupported-media-type-status-exception",
        "Media Type: text/plain Not Supported, Supported Media Types are: application/json");
    assertThat(result).headers().hasValue(HttpHeaders.ACCEPT, "application/json");
  }

  @Test
  void noSupportedMediaTypesSendsNoAcceptHeader() {
    final MvcTestResult result = this.mvc.get().uri("/http/webflux-no-media-types").exchange();
    assertProblem(result, 415, "unsupported-media-type-status-exception");
    assertThat(result).headers().doesNotContainHeader(HttpHeaders.ACCEPT);
  }

  @Test
  void notAcceptable() {
    final MvcTestResult result =
        this.mvc.get().uri("/http/json-only").accept(MediaType.APPLICATION_XML).exchange();
    assertProblem(
        result,
        406,
        "http-media-type-not-acceptable-exception",
        "Media Type Not Acceptable, except: application/json");
    assertThat(result).headers().hasValue(HttpHeaders.ACCEPT, "application/json");
  }

  @Test
  void notAcceptableStatusException() {
    final MvcTestResult result = this.mvc.get().uri("/http/webflux-not-acceptable").exchange();
    assertProblem(
        result,
        406,
        "not-acceptable-status-exception",
        "Media Type Not Acceptable, except: application/json");
    assertThat(result).headers().hasValue(HttpHeaders.ACCEPT, "application/json");
  }

  @Test
  void notAcceptableStatusExceptionWithOnlyReasonSendsNoAcceptHeader() {
    final MvcTestResult result =
        this.mvc.get().uri("/http/webflux-not-acceptable-reason").exchange();
    assertProblem(
        result, 406, "not-acceptable-status-exception", "Media Type Not Acceptable, except: ");
    assertThat(result).headers().doesNotContainHeader(HttpHeaders.ACCEPT);
  }
}
