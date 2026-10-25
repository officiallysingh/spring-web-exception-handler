package com.neolama.common.exception.advice.routing;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Requests that reach no handler, or miss a required parameter, header, part, or cookie. */
@AdviceTest
class RoutingAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void unknownPathIs404() {
    assertProblem(
            this.mvc.get().uri("/does-not-exist").exchange(),
            404,
            "no-resource-found-exception",
            "No resource found for GET /does-not-exist")
        .extractingPath("$.instance")
        .isEqualTo("/does-not-exist");
  }

  @Test
  void missingParameterUsesParameterKey() {
    assertProblem(
        this.mvc.get().uri("/routing/param").exchange(),
        400,
        "bad-request",
        "Query parameter page is required");
  }

  @Test
  void missingHeader() {
    assertProblem(
        this.mvc.get().uri("/routing/header").exchange(),
        400,
        "bad-request",
        "Required request header 'X-Tenant' for method parameter type String is not present");
  }

  @Test
  void missingPart() {
    assertProblem(
        this.mvc
            .post()
            .uri("/routing/part")
            .multipart()
            .file(new MockMultipartFile("other", "a.txt", "text/plain", "x".getBytes()))
            .exchange(),
        400,
        "bad-request",
        "Required part 'file' is not present.");
  }

  @Test
  void missingCookieIsARequestBindingError() {
    assertProblem(
        this.mvc.get().uri("/routing/cookie").exchange(),
        400,
        "missing-request-cookie-exception",
        "Required cookie 'session' for method parameter type String is not present");
  }

  @Nested
  @TestPropertySource(properties = "spring.web.resources.add-mappings=false")
  class WithoutStaticResources {

    @Autowired private MockMvcTester mvc;

    @Test
    void unknownPathIsNoHandlerFound() {
      assertProblem(
          this.mvc.get().uri("/does-not-exist").exchange(), 404, "no-handler-found-exception");
    }
  }
}
