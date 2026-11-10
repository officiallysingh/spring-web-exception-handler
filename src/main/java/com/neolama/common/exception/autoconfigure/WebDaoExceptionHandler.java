package com.neolama.common.exception.autoconfigure;

import com.neolama.common.exception.advice.dao.DaoAdvice;
import com.neolama.common.exception.dao.ConcurrencyFailureDetector;
import com.neolama.common.exception.dao.constraint.ConstraintNameResolver;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice that turns data-integrity, duplicate-key, and concurrency failures into problem
 * details.
 *
 * <p>Uses the first {@link ConstraintNameResolver} that supports the most specific cause, so
 * PostgreSQL JDBC and MongoDB driver errors each reach their own resolver. Errors from any other
 * source get the exception simple name as error key. Whether an exception is a concurrency failure
 * is the {@link ConcurrencyFailureDetector}'s answer, so the response and an application retry
 * agree. Enabled in servlet web applications that have {@code spring-tx} on the classpath, when
 * {@code problem.dao-advice-enabled} is {@code true} or unset.
 *
 * <p>Has default precedence, so an application's own advice for the same exceptions wins. Ordered
 * before {@link WebExceptionHandler}, whose {@code Throwable} fallback would otherwise catch these
 * exceptions first.
 */
@AutoConfiguration(before = WebExceptionHandler.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DataIntegrityViolationException.class)
@ConditionalOnBean(ConcurrencyFailureDetector.class)
@ConditionalOnProperty(
    prefix = "problem",
    name = "dao-advice-enabled",
    havingValue = "true",
    matchIfMissing = true)
@RestControllerAdvice
public class WebDaoExceptionHandler implements DaoAdvice {

  /** Resolvers consulted in order; the first that supports the cause is used. */
  protected final List<ConstraintNameResolver> constraintNameResolvers;

  /** Decides which exceptions are answered as concurrency failures. */
  protected final ConcurrencyFailureDetector concurrencyFailureDetector;

  /**
   * Creates a handler that consults the given constraint-name resolvers and concurrency failure
   * detector.
   *
   * @param constraintNameResolvers the resolvers to consult, possibly empty
   * @param concurrencyFailureDetector the detector that decides concurrency failures
   */
  protected WebDaoExceptionHandler(
      final List<ConstraintNameResolver> constraintNameResolvers,
      final ConcurrencyFailureDetector concurrencyFailureDetector) {
    this.constraintNameResolvers = List.copyOf(constraintNameResolvers);
    this.concurrencyFailureDetector = concurrencyFailureDetector;
  }

  /** {@inheritDoc} */
  @Override
  public @Nullable String resolveConstraintName(final Throwable cause) {
    return resolverFor(cause).map(resolver -> resolver.resolveConstraintName(cause)).orElse(null);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUniqueViolation(final Throwable cause) {
    return resolverFor(cause).map(resolver -> resolver.isUniqueViolation(cause)).orElse(false);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isConcurrencyFailure(final Throwable error) {
    return this.concurrencyFailureDetector.isConcurrencyFailure(error);
  }

  /**
   * Returns the first resolver that supports the given cause.
   *
   * @param cause the most specific cause of a data-access exception
   * @return the resolver, or empty when none supports the cause
   */
  private Optional<ConstraintNameResolver> resolverFor(final Throwable cause) {
    return this.constraintNameResolvers.stream()
        .filter(resolver -> resolver.supports(cause))
        .findFirst();
  }
}
