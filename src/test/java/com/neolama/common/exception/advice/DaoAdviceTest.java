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
    properties = "spring.messages.basename=i18n/test-dao")
@AutoConfigureMockMvc
class DaoAdviceTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void causeWithNullMessageAndNoResolverGetsGenericKey() {
    assertThat(this.mvc.get().uri("/data-integrity-null-message"))
        .hasStatus(500)
        .bodyJson()
        .satisfies(
            body -> {
              body.assertThat()
                  .extractingPath("$.code")
                  .isEqualTo("data-integrity-violation-exception");
              body.assertThat()
                  .extractingPath("$.detail")
                  .isEqualTo("The request conflicts with existing data");
            });
  }

  @Test
  void notNullViolationGetsGenericKeyNotDuplicateKey() {
    assertThat(this.mvc.get().uri("/not-null-violation"))
        .hasStatus(500)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo("data-integrity-violation-exception");
  }

  @Test
  void postgresDuplicateKeyUsesConstraintKey() {
    assertThat(this.mvc.get().uri("/duplicate-key-postgres"))
        .hasStatus(409)
        .bodyJson()
        .satisfies(
            body -> {
              body.assertThat().extractingPath("$.code").isEqualTo("employee-name-taken");
              body.assertThat()
                  .extractingPath("$.detail")
                  .isEqualTo("Employee name must be unique");
            });
  }
}
