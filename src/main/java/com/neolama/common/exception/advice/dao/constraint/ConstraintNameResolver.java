package com.neolama.common.exception.advice.dao.constraint;

import org.jspecify.annotations.Nullable;

/** Extracts a database constraint name from a persistence exception. */
public interface ConstraintNameResolver {

  /**
   * Returns whether this resolver understands the given database exception.
   *
   * @param cause the most specific cause of the data-access exception
   * @return {@code true} when {@link #resolveConstraintName(Throwable)} applies to the cause
   */
  boolean supports(final Throwable cause);

  /**
   * Resolves the constraint name from the given database exception.
   *
   * @param cause the most specific cause of the data-access exception, already accepted by {@link
   *     #supports(Throwable)}
   * @return the constraint name, or {@code null} when the exception names no constraint
   */
  @Nullable String resolveConstraintName(final Throwable cause);
}
