package com.neolama.apps.useradvice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** An application's own advice must win over the library advices for the exceptions it handles. */
@SpringBootTest(classes = UserAdviceTestApplication.class)
@AutoConfigureMockMvc
class UserAdviceOverrideTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void userAdviceWinsOverDaoAdvice() {
    assertThat(this.mvc.get().uri("/user/data-integrity")).hasStatus(418);
  }

  @Test
  void userAdviceWinsOverSecurityAdvice() {
    assertThat(this.mvc.get().uri("/user/access-denied")).hasStatus(418);
  }

  @Test
  void userAdviceWinsOverThrowableFallback() {
    assertThat(this.mvc.get().uri("/user/illegal-state")).hasStatus(418);
  }
}
