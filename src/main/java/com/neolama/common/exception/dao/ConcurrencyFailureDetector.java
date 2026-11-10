package com.neolama.common.exception.dao;

import com.neolama.common.exception.dao.constraint.ConstraintNameResolver;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Tells whether an exception is a lost race with a concurrent writer, one that a retry can win.
 *
 * <p>An error is a concurrency failure when:
 *
 * <ul>
 *   <li>any throwable in its cause chain is a {@link ConcurrencyFailureException}, which covers
 *       optimistic-locking failures such as an {@code @Version} mismatch and pessimistic-locking
 *       failures such as a lock timeout or deadlock; or
 *   <li>the first {@link ConstraintNameResolver} that supports its most specific cause reports
 *       {@link ConstraintNameResolver#isConcurrencyFailure(Throwable) a concurrency failure}. For
 *       MongoDB that is a {@code WriteConflict} or an error labelled {@code
 *       TransientTransactionError}, but never a duplicate key or an error labelled {@code
 *       UnknownTransactionCommitResult}.
 * </ul>
 *
 * <p>The web advice answers exactly these errors as concurrency failures, so an HTTP response and
 * an application retry agree. Auto-configured in any application with {@code spring-tx} on the
 * classpath, with the auto-configured resolvers; an application's own bean of this type replaces
 * it, also for the web advice. A cause chain that loops is walked once.
 *
 * @author Rajveer Singh
 */
public class ConcurrencyFailureDetector {

  /** Resolvers consulted in order; the first that supports the most specific cause is used. */
  private final List<ConstraintNameResolver> constraintNameResolvers;

  /**
   * Creates a detector that consults the given resolvers.
   *
   * @param constraintNameResolvers the resolvers to consult in order, possibly empty
   */
  public ConcurrencyFailureDetector(final List<ConstraintNameResolver> constraintNameResolvers) {
    this.constraintNameResolvers = List.copyOf(constraintNameResolvers);
  }

  /**
   * Returns whether the given error is a lost race that a retry can win.
   *
   * @param error the error to inspect, possibly {@code null}
   * @return {@code true} when the error is a concurrency failure; {@code false} for {@code null}
   */
  public boolean isConcurrencyFailure(final @Nullable Throwable error) {
    if (error == null) {
      return false;
    }
    // Stops at the first repeated throwable, so a cause chain that loops ends.
    final List<Throwable> chain = ExceptionUtils.getThrowableList(error);
    if (chain.stream().anyMatch(ConcurrencyFailureException.class::isInstance)) {
      return true;
    }
    final Throwable cause = chain.getLast();
    return this.constraintNameResolvers.stream()
        .filter(resolver -> resolver.supports(cause))
        .findFirst()
        .map(resolver -> resolver.isConcurrencyFailure(cause))
        .orElse(false);
  }
}
