package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Configured detail messages must have their placeholders filled in. */
@SpringBootTest(
    classes = TestApplication.class,
    properties = "spring.messages.basename=i18n/test-detail-args,i18n/problems")
@AutoConfigureMockMvc
class DetailArgsTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void dataBufferLimitDetailShowsParsedSize() {
    assertThat(this.mvc.get().uri("/data-buffer-limit"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Upload limit is 1024B");
  }

  @Test
  void methodNotSupportedDetailShowsMethods() {
    assertThat(this.mvc.post().uri("/data-buffer-limit"))
        .hasStatus(405)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Use GET instead of POST");
  }

  @Test
  void shippedMaxUploadSizeDetailShowsLimit() {
    assertThat(this.mvc.get().uri("/max-upload-size"))
        .hasStatus(400)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Upload file size exceeded the maximum allowed limit: 1048576B");
  }
}
