package com.neolama.common.exception.advice.dao;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Concurrency failures: an optimistic-lock conflict is a {@code 409}; lock timeouts, deadlocks, and
 * other contention are a temporary {@code 503}.
 */
@AdviceTest
class ConcurrencyFailureAdviceTest {

  private static final String STALE =
      "The resource was changed by another request; reload it and try again";

  private static final String RETRY =
      "The request could not be completed because of concurrent activity; please retry";

  @Autowired private MockMvcTester mvc;

  @Test
  void optimisticLockIsConflict() {
    assertProblem(
        this.mvc.get().uri("/dao/optimistic-lock").exchange(),
        409,
        "optimistic-locking-failure-exception",
        STALE);
  }

  /** Same key and response as on MongoDB, so one bundle fits JPA and MongoDB. */
  @Test
  void optimisticLockSubclassIsAnsweredAsOptimisticLock() {
    assertProblem(
        this.mvc.get().uri("/dao/optimistic-lock-jpa").exchange(),
        409,
        "optimistic-locking-failure-exception",
        STALE);
  }

  @Test
  void lockTimeoutIsServiceUnavailable() {
    assertProblem(
        this.mvc.get().uri("/dao/lock-timeout").exchange(),
        503,
        "pessimistic-locking-failure-exception",
        RETRY);
  }

  /** The test bundle sets status 429 for this key, which wins over the 503 default. */
  @Test
  void configuredStatusWins() {
    assertProblem(
        this.mvc.get().uri("/dao/concurrency-failure").exchange(),
        429,
        "concurrency-failure-exception",
        RETRY);
  }

  /**
   * A lost write conflict arrives as a data-integrity violation; it gets the plain concurrency key,
   * whose configured 429 wins over the 503 default.
   */
  @Test
  void mongoWriteConflictIsConcurrencyFailure() {
    assertProblem(
        this.mvc.get().uri("/dao/write-conflict-mongo").exchange(),
        429,
        "concurrency-failure-exception",
        RETRY);
  }

  /** An aborted transaction arrives as a transient exception; same answer as a write conflict. */
  @Test
  void mongoTransientTransactionIsConcurrencyFailure() {
    assertProblem(
        this.mvc.get().uri("/dao/transient-transaction-mongo").exchange(),
        429,
        "concurrency-failure-exception",
        RETRY);
  }

  @Test
  void otherTransientFailureIsAnsweredAsFallback() {
    assertProblem(
        this.mvc.get().uri("/dao/transient-other").exchange(),
        500,
        "transient-session-exception",
        "An unexpected error occurred");
  }

  @Test
  void unknownCommitResultIsAnsweredAsFallback() {
    assertProblem(
        this.mvc.get().uri("/dao/unknown-commit-result-mongo").exchange(),
        500,
        "transient-session-exception",
        "An unexpected error occurred");
  }

  @Test
  void transientDuplicateStaysDuplicate() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-mongo-transient").exchange(),
        409,
        "conflict",
        "State name must be unique");
  }

  @Nested
  @TestPropertySource(properties = "problem.dao-advice-enabled=false")
  class Disabled {

    @Autowired private MockMvcTester mvc;

    @Test
    void fallbackHandlesConcurrencyFailures() {
      assertProblem(
          this.mvc.get().uri("/dao/optimistic-lock").exchange(),
          500,
          "optimistic-locking-failure-exception",
          "An unexpected error occurred");
    }
  }
}
