package com.neolama.common.exception.advice.dao.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;

class PostgresConstraintNameResolverTest {

  private final PostgresConstraintNameResolver resolver = new PostgresConstraintNameResolver();

  @Test
  void usesStructuredConstraintField() {
    // Server error fields: S = severity, C = SQLState, M = message, n = constraint name.
    final ServerErrorMessage serverError =
        new ServerErrorMessage(
            "SERROR\0C23505\0Mduplicate key value violates unique constraint \"from_message\"\0"
                + "nfrom_structured_field\0");
    assertThat(this.resolver.resolveConstraintName(new PSQLException(serverError)))
        .isEqualTo("from_structured_field");
  }

  @Test
  void fallsBackToMessage() {
    final PSQLException cause =
        new PSQLException(
            "ERROR: insert or update on table \"orders\" violates foreign key constraint"
                + " \"orders_customer_fk\"",
            PSQLState.FOREIGN_KEY_VIOLATION);
    assertThat(this.resolver.resolveConstraintName(cause)).isEqualTo("orders_customer_fk");
  }

  @Test
  void notNullViolationHasNoConstraintName() {
    final PSQLException cause =
        new PSQLException(
            "ERROR: null value in column \"name\" of relation \"employees\" violates not-null"
                + " constraint",
            PSQLState.NOT_NULL_VIOLATION);
    assertThat(this.resolver.resolveConstraintName(cause)).isNull();
  }

  @Test
  void messageWithoutConstraintDoesNotReturnJunk() {
    final PSQLException cause =
        new PSQLException(
            "ERROR: column \"x\" of relation \"y\" does not exist", PSQLState.UNDEFINED_COLUMN);
    assertThat(this.resolver.resolveConstraintName(cause)).isNull();
  }

  @Test
  void nullMessageReturnsNull() {
    assertThat(this.resolver.resolveConstraintName(new PSQLException(null, null))).isNull();
  }

  @Test
  void blankStructuredConstraintFallsBackToMessage() {
    final ServerErrorMessage serverError =
        new ServerErrorMessage(
            "SERROR\0C23505\0Mduplicate key value violates unique constraint \"from_message\"\0");
    assertThat(this.resolver.resolveConstraintName(new PSQLException(serverError)))
        .isEqualTo("from_message");
  }

  @Test
  void uniqueViolationIsRecognisedBySqlState() {
    assertThat(
            this.resolver.isUniqueViolation(
                new PSQLException("duplicate key", PSQLState.UNIQUE_VIOLATION)))
        .isTrue();
    assertThat(
            this.resolver.isUniqueViolation(
                new PSQLException("violates foreign key", PSQLState.FOREIGN_KEY_VIOLATION)))
        .isFalse();
    assertThat(this.resolver.isUniqueViolation(new PSQLException(null, null))).isFalse();
  }

  @Test
  void readsMessageOfOtherSqlExceptions() {
    assertThat(
            this.resolver.resolveConstraintName(
                new java.sql.SQLException("violates unique constraint \"orders_ref_key\"")))
        .isEqualTo("orders_ref_key");
  }
}
