package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Configured field violation messages can use the field name and constraint attributes. */
@SpringBootTest(
    classes = TestApplication.class,
    properties = "spring.messages.basename=i18n/test-field-args")
@AutoConfigureMockMvc
class FieldErrorArgumentsTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void constraintAttributesFillPlaceholders() {
    assertThat(
            this.mvc
                .post()
                .uri("/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"x\",\"nick\":\"toolongnick\"}"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.violations[0].message")
        .isEqualTo("Nick nick must be 2 to 5 characters, got 'toolongnick'");
  }
}
