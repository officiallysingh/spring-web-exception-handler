package com.neolama.common.exception.advice.dao.constraint;

import org.postgresql.util.PSQLException;

/**
 * {@link ConstraintNameResolver} that reads a constraint name from a PostgreSQL JDBC driver
 * exception message.
 */
public class PostgresConstraintNameResolver implements ConstraintNameResolver {

  /** {@inheritDoc} */
  @Override
  public String resolveConstraintName(final String exceptionMessage) {
    String exMessage = exceptionMessage.trim();
    try {
      exMessage = exMessage.substring(exMessage.indexOf("constraint") + 12);
      return exMessage.substring(0, exMessage.indexOf("\""));
    } catch (final Exception e) {
      // Ignored on purpose
    }
    return "postgres.duplicate.key";
  }

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Throwable cause) {
    return cause instanceof PSQLException;
  }
}
