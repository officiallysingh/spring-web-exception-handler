package com.neolama.apps.webmvc;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * {@code @WebMvcTest} picks up the library without {@code @ImportAutoConfiguration}, through {@code
 * META-INF/spring/org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc.imports}.
 */
@WebMvcTest(
    controllers = WebMvcSliceController.class,
    properties = "problem.type-url=https://example.com/problems")
class WebMvcSliceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void unhandledExceptionIsProblem() {
    assertProblem(
            this.mvc.get().uri("/slice/illegal-state").exchange(),
            500,
            "illegal-state-exception",
            "An unexpected error occurred")
        .extractingPath("$.type")
        .isEqualTo("https://example.com/problems#illegal-state-exception");
  }

  @Test
  void databaseErrorUsesDaoHandler() {
    assertProblem(
        this.mvc.get().uri("/slice/duplicate-key").exchange(),
        409,
        "duplicate-key-exception",
        "The request conflicts with existing data");
  }

  @Test
  void securityErrorUsesSecurityHandler() {
    assertProblem(
        this.mvc.get().uri("/slice/access-denied").exchange(),
        403,
        "access-denied-exception",
        "Access is denied");
  }
}
