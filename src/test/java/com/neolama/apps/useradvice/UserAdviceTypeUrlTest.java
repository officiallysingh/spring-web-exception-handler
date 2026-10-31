package com.neolama.apps.useradvice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** An application's own advice built on {@code Exceptional} gets the type from the property. */
@SpringBootTest(
    classes = UserAdviceTestApplication.class,
    properties = "problem.type-url=https://example.com/problems")
@AutoConfigureMockMvc
class UserAdviceTypeUrlTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void userAdviceSetsTypeFromProperty() {
    assertThat(this.mvc.get().uri("/user/payment-declined"))
        .hasStatus(402)
        .bodyJson()
        .extractingPath("$.type")
        .isEqualTo("https://example.com/problems#payment-declined-exception");
  }
}
