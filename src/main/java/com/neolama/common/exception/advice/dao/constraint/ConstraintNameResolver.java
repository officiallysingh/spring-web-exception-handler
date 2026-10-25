package com.neolama.common.exception.advice.dao.constraint;

/** Extracts a database constraint name from a persistence exception message. */
public interface ConstraintNameResolver {

  /**
   * Returns whether this resolver understands the given database exception.
   *
   * @param cause the most specific cause of the data-access exception
   * @return {@code true} when {@link #resolveConstraintName(String)} applies to the cause
   */
  boolean supports(final Throwable cause);

  /**
   * Resolves the constraint name from the given exception message.
   *
   * @param exceptionMessage the database exception message, never {@code null}
   * @return the extracted constraint name, or a default value if not found
   */
  String resolveConstraintName(final String exceptionMessage);
}
