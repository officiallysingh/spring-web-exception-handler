package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Every problem response has the same RFC 9457 shape. */
@AdviceTest
class ResponseShapeTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void isProblemJsonWithFieldsInOrder() {
    assertThat(this.mvc.get().uri("/general/problem"))
        .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
        .bodyText()
        .containsSubsequence(
            "\"type\"",
            "\"status\"",
            "\"instance\"",
            "\"method\"",
            "\"code\"",
            "\"title\"",
            "\"detail\"",
            "\"timestamp\"");
  }

  @Test
  void violationsComeBeforeAnyCustomProperty() {
    assertThat(this.mvc.get().uri("/validation/page").param("page", "-1").param("size", "1"))
        .bodyText()
        .containsSubsequence("\"timestamp\"", "\"violations\"");
  }

  @Test
  void unsetListsAreLeftOut() {
    assertThat(this.mvc.get().uri("/general/problem"))
        .bodyJson()
        .doesNotHavePath("$.violations")
        .doesNotHavePath("$.errors");
  }

  @Test
  void typeDefaultsToAboutBlank() {
    assertThat(this.mvc.get().uri("/general/problem"))
        .bodyJson()
        .extractingPath("$.type")
        .isEqualTo("about:blank");
  }

  @Test
  void timestampIsIsoUtc() {
    assertThat(this.mvc.get().uri("/general/problem"))
        .bodyJson()
        .extractingPath("$.timestamp")
        .asString()
        .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z");
  }
}
