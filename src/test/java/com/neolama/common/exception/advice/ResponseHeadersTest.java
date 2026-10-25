package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** 405 responses carry {@code Allow}; 415 and 406 responses carry {@code Accept}. */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class ResponseHeadersTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void methodNotAllowedSetsAllow() {
    assertThat(this.mvc.post().uri("/data-buffer-limit"))
        .hasStatus(405)
        .headers()
        .hasHeaderSatisfying(
            HttpHeaders.ALLOW, values -> assertThat(String.join(",", values)).contains("GET"));
  }

  @Test
  void unsupportedMediaTypeSetsAccept() {
    assertThat(this.mvc.post().uri("/validated").contentType(MediaType.TEXT_PLAIN).content("name"))
        .hasStatus(415)
        .headers()
        .hasHeaderSatisfying(
            HttpHeaders.ACCEPT,
            values -> assertThat(String.join(",", values)).contains("application/json"));
  }

  @Test
  void notAcceptableSetsAccept() {
    assertThat(this.mvc.get().uri("/json-only").accept(MediaType.APPLICATION_XML))
        .hasStatus(406)
        .headers()
        .hasValue(HttpHeaders.ACCEPT, "application/json");
  }

  @Test
  void otherErrorsGetNoExtraHeaders() {
    assertThat(this.mvc.get().uri("/illegal-state"))
        .hasStatus(500)
        .headers()
        .doesNotContainHeaders(HttpHeaders.ALLOW, HttpHeaders.ACCEPT);
  }
}
