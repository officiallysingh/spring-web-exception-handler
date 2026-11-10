package com.neolama.common.exception.advice.dao.constraint;

import org.jspecify.annotations.Nullable;

/**
 * Reads a database driver exception: the constraint it names and whether it is a unique violation.
 */
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

  /**
   * Returns whether the given database exception reports a unique-constraint or unique-index
   * violation.
   *
   * <p>Spring does not always translate such an error into a {@link
   * org.springframework.dao.DuplicateKeyException}: under JPA, Hibernate reports it as a plain
   * {@link org.springframework.dao.DataIntegrityViolationException}. The driver error tells them
   * apart.
   *
   * @param cause the most specific cause of the data-access exception, already accepted by {@link
   *     #supports(Throwable)}
   * @return {@code true} when the cause is a unique violation
   */
  boolean isUniqueViolation(final Throwable cause);

  /**
   * Returns whether the given database exception reports a lost race with a concurrent writer that
   * a retry can win.
   *
   * <p>Spring does not always translate such an error into a {@link
   * org.springframework.dao.ConcurrencyFailureException}: Spring Data MongoDB reports a write
   * conflict as a {@link org.springframework.dao.DataIntegrityViolationException}, and an aborted
   * transaction as a {@link org.springframework.dao.TransientDataAccessException}. The driver error
   * tells them apart.
   *
   * @param cause the most specific cause of the data-access exception, already accepted by {@link
   *     #supports(Throwable)}
   * @return {@code true} when the cause is a concurrency failure; {@code false} by default
   */
  default boolean isConcurrencyFailure(final Throwable cause) {
    return false;
  }
}
