package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(
    classes = TestApplication.class,
    properties = "spring.messages.basename=i18n/test-status-override")
@AutoConfigureMockMvc
class StatusOverrideTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void configuredStatusOverridesErrorResponseStatus() {
    assertThat(this.mvc.get().uri("/does-not-exist")).hasStatus(410);
  }
}
