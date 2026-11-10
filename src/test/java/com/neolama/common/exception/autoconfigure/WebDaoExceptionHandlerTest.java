package com.neolama.common.exception.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoException;
import com.neolama.common.exception.dao.ConcurrencyFailureDetector;
import com.neolama.common.exception.dao.constraint.ConstraintNameResolver;
import com.neolama.common.exception.dao.constraint.MongoConstraintNameResolver;
import com.neolama.common.exception.dao.constraint.PostgresConstraintNameResolver;
import java.sql.SQLException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataIntegrityViolationException;

class WebDaoExceptionHandlerTest {

  private static final List<ConstraintNameResolver> RESOLVERS =
      List.of(new PostgresConstraintNameResolver(), new MongoConstraintNameResolver());

  private final WebDaoExceptionHandler handler =
      new WebDaoExceptionHandler(RESOLVERS, new ConcurrencyFailureDetector(RESOLVERS));

  @Test
  void handlerWithoutResolversReturnsNull() {
    final WebDaoExceptionHandler noResolvers =
        new WebDaoExceptionHandler(List.of(), new ConcurrencyFailureDetector(List.of()));
    assertThat(noResolvers.resolveConstraintName(new SQLException("boom"))).isNull();
  }

  @Test
  void postgresCauseGoesToPostgresResolver() {
    final PSQLException cause =
        new PSQLException(
            "ERROR: duplicate key value violates unique constraint \"employees_name_key\"",
            PSQLState.UNIQUE_VIOLATION);
    assertThat(this.handler.resolveConstraintName(cause)).isEqualTo("employees_name_key");
  }

  @Test
  void mongoCauseGoesToMongoResolver() {
    final MongoException cause =
        new MongoException(
            11000,
            "E11000 duplicate key error collection: db.states index: name_1 dup key: { name: \"x\" }");
    assertThat(this.handler.resolveConstraintName(cause)).isEqualTo("states.name_1");
  }

  @Test
  void unsupportedCauseReturnsNull() {
    assertThat(this.handler.resolveConstraintName(new SQLException("H2 error"))).isNull();
  }

  @Test
  void uniqueViolationIsAskedOfTheSupportingResolver() {
    assertThat(
            this.handler.isUniqueViolation(
                new PSQLException("duplicate key", PSQLState.UNIQUE_VIOLATION)))
        .isTrue();
    assertThat(this.handler.isUniqueViolation(new MongoException(11000, "E11000"))).isTrue();
    assertThat(
            this.handler.isUniqueViolation(
                new PSQLException("not null", PSQLState.NOT_NULL_VIOLATION)))
        .isFalse();
    assertThat(this.handler.isUniqueViolation(new SQLException("H2 error", "23505"))).isFalse();
  }

  @Test
  void concurrencyFailureIsAskedOfTheDetector() {
    final WriteConflictException error =
        new WriteConflictException(new MongoException(112, "WriteConflict"));
    assertThat(this.handler.isConcurrencyFailure(error)).isTrue();
    assertThat(this.handler.isConcurrencyFailure(new SQLException("H2 error"))).isFalse();
  }

  /** An application's own detector decides for the advice too. */
  @Test
  void replacedDetectorDecides() {
    final ConcurrencyFailureDetector everything =
        new ConcurrencyFailureDetector(List.of()) {
          @Override
          public boolean isConcurrencyFailure(final @Nullable Throwable error) {
            return true;
          }
        };
    final WebDaoExceptionHandler replaced = new WebDaoExceptionHandler(RESOLVERS, everything);
    assertThat(replaced.isConcurrencyFailure(new SQLException("H2 error"))).isTrue();
  }

  /** What Spring Data MongoDB throws for a write conflict. */
  private static final class WriteConflictException extends DataIntegrityViolationException {

    WriteConflictException(final MongoException cause) {
      super(cause.getMessage(), cause);
    }
  }

  @Test
  void nullMessageDoesNotThrow() {
    assertThat(this.handler.resolveConstraintName(new MongoException((String) null))).isNull();
  }
}
