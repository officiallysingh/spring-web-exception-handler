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

  private static final String CONFLICT = "The request conflicts with existing data";

  @Autowired private MockMvcTester mvc;

  @Test
  void configuredPostgresConstraint() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-postgres").exchange(),
        409,
        "employee-name-taken",
        "Employee name must be unique");
  }

  @Test
  void configuredMongoIndex() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-mongo").exchange(),
        500,
        "internal-server-error",
        "State name must be unique");
  }

  @Test
  void unconfiguredConstraintHidesDriverMessage() {
    assertProblem(
        this.mvc.get().uri("/dao/duplicate-postgres-unconfigured").exchange(),
        500,
        "internal-server-error",
        CONFLICT);
  }

  @Test
  void violationWithoutConstraintNameGetsGenericKey() {
    assertProblem(
        this.mvc.get().uri("/dao/not-null-postgres").exchange(),
        500,
        "data-integrity-violation-exception",
        CONFLICT);
  }

  @Test
  void causeWithNullMessageDoesNotFail() {
    assertProblem(
        this.mvc.get().uri("/dao/null-message").exchange(),
        500,
        "data-integrity-violation-exception",
        CONFLICT);
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
          "An unexpected error occurred");
    }
  }
}
