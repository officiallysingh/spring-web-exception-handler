package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class ErrorResponseStatusTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void unknownPathReturns404() {
    assertThat(this.mvc.get().uri("/does-not-exist"))
        .hasStatus(404)
        .bodyJson()
        .satisfies(
            body -> {
              body.assertThat().extractingPath("$.code").isEqualTo("no-resource-found-exception");
              body.assertThat().extractingPath("$.status").isEqualTo(404);
              body.assertThat().extractingPath("$.instance").isEqualTo("/does-not-exist");
              body.assertThat().extractingPath("$.method").isEqualTo("GET");
              body.assertThat()
                  .extractingPath("$.detail")
                  .isEqualTo("No resource found for GET /does-not-exist");
            });
  }

  @Test
  void errorResponseWithoutDedicatedAdviceUsesItsOwnStatus() {
    assertThat(this.mvc.get().uri("/async-timeout"))
        .hasStatus(503)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo("async-request-timeout-exception");
  }
}
