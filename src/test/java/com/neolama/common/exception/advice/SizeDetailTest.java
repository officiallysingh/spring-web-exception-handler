package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Size limits are reported as readable sizes when no detail is configured. */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class SizeDetailTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void tomcatFileSizeLimitUsesPermittedSizeNotFieldNameDigits() {
    assertThat(this.mvc.get().uri("/upload-file-size-tomcat"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Upload file size exceeded the maximum allowed limit: 1 MB");
  }

  @Test
  void tomcatRequestSizeLimitUsesConfiguredMaximumNotActualSize() {
    assertThat(this.mvc.get().uri("/upload-request-size-tomcat"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Upload file size exceeded the maximum allowed limit: 1 MB");
  }

  @Test
  void knownMaxUploadSizeIsReadable() {
    assertThat(this.mvc.get().uri("/max-upload-size"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Upload file size exceeded the maximum allowed limit: 1 MB");
  }

  @Test
  void dataBufferLimitIsReadable() {
    assertThat(this.mvc.get().uri("/data-buffer-limit"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Request data exceeded the allowed limit: 1 KB");
  }
}
