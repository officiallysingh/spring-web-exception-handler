package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Raw exception messages must not reach the client when no detail is configured. */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class SafeDetailTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void serverErrorHidesExceptionMessage() {
    assertThat(this.mvc.get().uri("/illegal-state"))
        .hasStatus(500)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("An unexpected error occurred");
  }

  @Test
  void duplicateKeyHidesDriverMessage() {
    assertThat(this.mvc.get().uri("/duplicate-key-postgres-unconfigured"))
        .hasStatus(500)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("The request conflicts with existing data");
  }

  @Test
  void invalidBodyHidesSpringDumpButKeepsViolations() {
    assertThat(
            this.mvc
                .post()
                .uri("/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .hasStatus(400)
        .bodyJson()
        .satisfies(
            body -> {
              body.assertThat().extractingPath("$.detail").isEqualTo("Request validation failed");
              body.assertThat().extractingPath("$.violations.length()").isEqualTo(1);
            });
  }

  @Test
  void malformedBodyHidesJacksonMessage() {
    assertThat(
            this.mvc
                .post()
                .uri("/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Malformed request body");
  }

  @Test
  void unknownUserReadsLikeBadCredentials() {
    assertThat(this.mvc.get().uri("/username-not-found"))
        .hasStatus(401)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Bad credentials");
  }
}
