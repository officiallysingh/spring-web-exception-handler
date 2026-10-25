package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(
    classes = TestApplication.class,
    properties = "spring.messages.basename=i18n/test-formats")
@AutoConfigureMockMvc
class InvalidFormatAdviceTest {

  @Autowired private MockMvcTester mvc;

  private MockMvcTester.MockMvcRequestBuilder post(final String json) {
    return this.mvc.post().uri("/formats").contentType(MediaType.APPLICATION_JSON).content(json);
  }

  @Test
  void dottedMapKeyReturnsProblemInsteadOfCrashing() {
    assertThat(post("{\"counts\":{\"a.b\":\"x\"}}"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Some value has the wrong format");
  }

  @Test
  void specificKeyIsUsed() {
    assertThat(post("{\"count\":\"x\"}"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Count must be a whole number");
  }

  @Test
  void plainKeyCatchesFieldWithoutSpecificEntry() {
    assertThat(post("{\"item\":{\"qty\":\"x\"}}"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Some value has the wrong format");
  }
}
