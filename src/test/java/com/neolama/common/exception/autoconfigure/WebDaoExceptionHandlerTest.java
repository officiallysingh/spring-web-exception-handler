package com.neolama.common.exception.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoException;
import com.neolama.common.exception.advice.dao.constraint.MongoConstraintNameResolver;
import com.neolama.common.exception.advice.dao.constraint.PostgresConstraintNameResolver;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

class WebDaoExceptionHandlerTest {

  private final WebDaoExceptionHandler handler =
      new WebDaoExceptionHandler(
          List.of(new PostgresConstraintNameResolver(), new MongoConstraintNameResolver()), "");

  @Test
  void handlerWithoutResolversReturnsNull() {
    final WebDaoExceptionHandler noResolvers = new WebDaoExceptionHandler(List.of(), "");
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
  void nullMessageDoesNotThrow() {
    assertThat(this.handler.resolveConstraintName(new MongoException((String) null))).isNull();
  }
}
