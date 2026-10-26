package com.neolama.common.exception.advice;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * A {@code status.} entry for the full error key, such as a parameter or property key, wins over
 * the advice's default status.
 */
@AdviceTest
class StatusKeyTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void missingParameter() {
    assertProblem(this.mvc.get().uri("/status-key/param").exchange(), 409, "conflict");
  }

  @Test
  void missingHeader() {
    assertProblem(this.mvc.get().uri("/status-key/header").exchange(), 409, "conflict");
  }

  @Test
  void missingPart() {
    assertProblem(
        this.mvc
            .post()
            .uri("/status-key/part")
            .multipart()
            .file(new MockMultipartFile("other", "x".getBytes()))
            .exchange(),
        409,
        "conflict");
  }

  @Test
  void methodArgumentTypeMismatch() {
    assertProblem(
        this.mvc.get().uri("/status-key/typed").param("since", "yesterday").exchange(),
        409,
        "conflict");
  }

  @Test
  void propertyTypeMismatch() {
    assertProblem(this.mvc.get().uri("/status-key/property").exchange(), 409, "conflict");
  }

  @Test
  void invalidJsonValue() {
    assertProblem(
        this.mvc
            .post()
            .uri("/status-key/body")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"age\":\"old\"}")
            .exchange(),
        409,
        "conflict");
  }
}
