package com.neolama.common.exception.advice.security;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Spring Security exceptions thrown inside a controller. */
@AdviceTest
class SecurityAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void accessDeniedIs403() {
    assertProblem(
        this.mvc.get().uri("/security/access-denied").exchange(),
        403,
        "access-denied-exception",
        "Access is denied");
  }

  @Test
  void otherAuthenticationFailureIs401() {
    assertProblem(
        this.mvc.get().uri("/security/authentication").exchange(),
        401,
        "authentication-credentials-not-found-exception",
        "No credentials");
  }

  @Test
  void badCredentialsIs401() {
    assertProblem(
        this.mvc.get().uri("/security/bad-credentials").exchange(),
        401,
        "bad-credentials-exception",
        "Bad credentials");
  }

  @Test
  void insufficientAuthenticationIs401() {
    assertProblem(
        this.mvc.get().uri("/security/insufficient").exchange(),
        401,
        "insufficient-authentication-exception",
        "Full authentication is required");
  }

  @Test
  void unknownUserReadsLikeBadCredentials() {
    assertProblem(
        this.mvc.get().uri("/security/username-not-found").exchange(),
        401,
        "bad-credentials-exception",
        "Bad credentials");
  }
}
