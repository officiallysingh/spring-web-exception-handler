package com.neolama.common.exception.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.http.MediaType;
import org.springframework.test.json.AbstractJsonContentAssert;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Assertions shared by the advice tests for RFC 9457 problem responses. */
public final class ProblemAssertions {

  private ProblemAssertions() {}

  /**
   * Asserts the response is a problem with the given status, code, and detail, and returns its JSON
   * body for further assertions.
   *
   * @param result the exchanged request
   * @param status the expected HTTP status, also expected in the body
   * @param code the expected problem code
   * @param detail the expected problem detail
   * @return assertions on the JSON body
   */
  public static AbstractJsonContentAssert<?> assertProblem(
      final MvcTestResult result, final int status, final String code, final String detail) {
    final AbstractJsonContentAssert<?> body = assertProblem(result, status, code);
    body.extractingPath("$.detail").isEqualTo(detail);
    return body;
  }

  /**
   * Asserts the response is a problem with the given status and code, and returns its JSON body.
   *
   * @param result the exchanged request
   * @param status the expected HTTP status, also expected in the body
   * @param code the expected problem code
   * @return assertions on the JSON body
   */
  public static AbstractJsonContentAssert<?> assertProblem(
      final MvcTestResult result, final int status, final String code) {
    assertThat(result).hasStatus(status).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    final AbstractJsonContentAssert<?> body = assertThat(result).bodyJson();
    body.extractingPath("$.status").isEqualTo(status);
    body.extractingPath("$.code").isEqualTo(code);
    return body;
  }
}
