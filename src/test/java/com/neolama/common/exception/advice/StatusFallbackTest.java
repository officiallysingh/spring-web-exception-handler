package com.neolama.common.exception.advice;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * A {@code status.} entry for the exception name applies when the more specific key has none, in
 * every advice that derives a longer key.
 */
@AdviceTest
@TestPropertySource(properties = "spring.messages.basename=i18n/status-fallback-messages")
class StatusFallbackTest {

  @Autowired private MockMvcTester mvc;

  /** {@code status.DuplicateKeyException} covers a constraint that has no entry of its own. */
  @Test
  void duplicateKeyStatusCoversEveryConstraint() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-mongo").exchange(), 422, "unprocessable-content");
  }

  /** The thrown class is tried before the Spring base class used for messages. */
  @Test
  void concurrencyStatusForThrownClass() {
    assertProblem(
        this.mvc.get().uri("/dao/lock-timeout").exchange(),
        423,
        "pessimistic-locking-failure-exception");
  }
}
