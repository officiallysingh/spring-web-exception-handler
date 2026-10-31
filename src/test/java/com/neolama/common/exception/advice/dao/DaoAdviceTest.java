package com.neolama.common.exception.advice.dao;

import static com.neolama.common.exception.support.ProblemAssertions.assertProblem;

import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Data-integrity failures. The error key is {@code {Exception}.{constraint}} when the constraint is
 * known, otherwise the exception name alone; raw database text never reaches the client.
 */
@AdviceTest
class DaoAdviceTest {

  private static final String DUPLICATE = "The request conflicts with existing data";

  private static final String GENERIC = "An unexpected error occurred";

  @Autowired private MockMvcTester mvc;

  /** The test bundle sets status 422 for this constraint, which wins over the 409 default. */
  @Test
  void configuredPostgresConstraint() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-postgres").exchange(),
        422,
        "employee-name-taken",
        "Employee name must be unique");
  }

  /** The test bundle sets no status for this index, so the duplicate default applies. */
  @Test
  void duplicateMongoIndexIsConflict() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-mongo").exchange(),
        409,
        "conflict",
        "State name must be unique");
  }

  @Test
  void unconfiguredDuplicateIsConflictAndHidesDriverMessage() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-postgres-unconfigured").exchange(),
        409,
        "conflict",
        DUPLICATE);
  }

  /** Same key and response as a {@code DuplicateKeyException}, so one bundle fits JDBC and JPA. */
  @Test
  void jpaUniqueViolationIsAnsweredAsDuplicateKey() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-postgres-jpa").exchange(),
        422,
        "employee-name-taken",
        "Employee name must be unique");
  }

  @Test
  void otherViolationIsServerErrorWithGenericDetail() {
    assertProblem(
        this.mvc.get().uri("/dao/not-null-postgres").exchange(),
        500,
        "data-integrity-violation-exception",
        GENERIC);
  }

  @Test
  void causeWithNullMessageDoesNotFail() {
    assertProblem(
        this.mvc.get().uri("/dao/null-message").exchange(),
        500,
        "data-integrity-violation-exception",
        GENERIC);
  }

  @Nested
  @TestPropertySource(properties = "problem.dao-advice-enabled=false")
  class Disabled {

    @Autowired private MockMvcTester mvc;

    @Test
    void fallbackHandlesDataAccessFailures() {
      assertProblem(
          this.mvc.get().uri("/dao/not-null-postgres").exchange(),
          500,
          "data-integrity-violation-exception",
          GENERIC);
    }
  }
}
