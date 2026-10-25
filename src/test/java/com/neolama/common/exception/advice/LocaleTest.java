package com.neolama.common.exception.advice;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Messages are resolved for the request's locale. */
@AdviceTest
class LocaleTest {

  @Autowired private MockMvcTester mvc;

  @Test
  void acceptLanguagePicksLocalizedBundle() {
    assertProblem(
        this.mvc
            .get()
            .uri("/general/localized")
            .header(HttpHeaders.ACCEPT_LANGUAGE, "fr")
            .exchange(),
        400,
        "bad-request",
        "Détail en français");
  }

  @Test
  void otherLanguageFallsBackToDefaultBundle() {
    assertProblem(
        this.mvc
            .get()
            .uri("/general/localized")
            .header(HttpHeaders.ACCEPT_LANGUAGE, Locale.JAPANESE.toLanguageTag())
            .exchange(),
        400,
        "bad-request",
        "Default language detail");
  }
}
