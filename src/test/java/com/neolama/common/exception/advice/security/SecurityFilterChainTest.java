package com.neolama.common.exception.advice.security;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;
import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.apps.secured.SecuredTestApplication;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Failures raised by the security filter chain, before any controller runs, reach the advices
 * through {@code ProblemAuthenticationEntryPoint} and {@code ProblemAccessDeniedHandler}.
 */
@SpringBootTest(classes = SecuredTestApplication.class)
@AutoConfigureMockMvc
class SecurityFilterChainTest {

  @Autowired private MockMvcTester mvc;

  private static String basic(final String user, final String password) {
    return "Basic "
        + Base64.getEncoder()
            .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void missingCredentialsIs401Problem() {
    assertProblem(
            this.mvc.get().uri("/secured/hello").exchange(),
            401,
            "insufficient-authentication-exception")
        .extractingPath("$.instance")
        .isEqualTo("/secured/hello");
  }

  @Test
  void wrongPasswordIs401Problem() {
    assertProblem(
        this.mvc
            .get()
            .uri("/secured/hello")
            .header(HttpHeaders.AUTHORIZATION, basic(SecuredTestApplication.USER, "wrong"))
            .exchange(),
        401,
        "bad-credentials-exception");
  }

  @Test
  void missingRoleIs403Problem() {
    assertProblem(
        this.mvc
            .get()
            .uri("/secured/admin")
            .header(
                HttpHeaders.AUTHORIZATION,
                basic(SecuredTestApplication.USER, SecuredTestApplication.PASSWORD))
            .exchange(),
        403,
        "authorization-denied-exception");
  }

  @Test
  void validCredentialsReachTheController() {
    assertThat(
            this.mvc
                .get()
                .uri("/secured/hello")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    basic(SecuredTestApplication.USER, SecuredTestApplication.PASSWORD)))
        .hasStatusOk()
        .hasBodyTextEqualTo("hello");
  }
}
