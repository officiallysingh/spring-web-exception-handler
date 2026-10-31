package com.neolama.common.exception.advice.dao.constraint;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;

/**
 * {@link ConstraintNameResolver} for PostgreSQL JDBC driver exceptions.
 *
 * <p>Uses the constraint name the server reports in its structured error fields. When those are
 * missing, reads it from a message such as {@code violates unique constraint "employees_name_key"}.
 * A unique violation is recognised by its SQLState, {@code 23505}.
 */
public class PostgresConstraintNameResolver implements ConstraintNameResolver {

  /** Matches {@code constraint "name"} and captures the name. */
  private static final Pattern CONSTRAINT = Pattern.compile("constraint \"([^\"]+)\"");

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Throwable cause) {
    return cause instanceof PSQLException;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUniqueViolation(final Throwable cause) {
    return cause instanceof SQLException exception
        && PSQLState.UNIQUE_VIOLATION.getState().equals(exception.getSQLState());
  }

  /** {@inheritDoc} */
  @Override
  public @Nullable String resolveConstraintName(final Throwable cause) {
    if (cause instanceof PSQLException exception) {
      final ServerErrorMessage serverError = exception.getServerErrorMessage();
      if (serverError != null && StringUtils.isNotBlank(serverError.getConstraint())) {
        return serverError.getConstraint();
      }
    }
    final String message = cause.getMessage();
    if (message == null) {
      return null;
    }
    final Matcher matcher = CONSTRAINT.matcher(message);
    return matcher.find() ? matcher.group(1) : null;
  }
}
