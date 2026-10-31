package com.neolama.common.exception.advice.http;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;
import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** 405 responses carry the {@code Allow} header when the supported methods are known. */
@AdviceTest
class MethodNotAllowedAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void mvcMethodNotSupportedUsesConfiguredDetailWithArguments() {
    final MvcTestResult result = this.mvc.post().uri("/http/get-only").exchange();
    assertProblem(
        result, 405, "http-request-method-not-supported-exception", "Use GET instead of POST");
    assertThat(result).headers().hasValue(HttpHeaders.ALLOW, "GET");
  }

  @Test
  void noSupportedMethodsSendsNoAllowHeader() {
    final MvcTestResult result = this.mvc.get().uri("/http/no-methods").exchange();
    assertProblem(
        result, 405, "http-request-method-not-supported-exception", "Use None instead of POST");
    assertThat(result).headers().doesNotContainHeader(HttpHeaders.ALLOW);
  }
}
