package com.neolama.common.exception.autoconfigure;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.advice.dao.DaoAdvice;
import com.neolama.common.exception.advice.dao.constraint.ConstraintNameResolver;
import java.net.URI;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice that turns data-integrity and duplicate-key failures into problem details.
 *
 * <p>Uses the first {@link ConstraintNameResolver} that supports the most specific cause, so
 * PostgreSQL JDBC and MongoDB driver errors each reach their own resolver. Errors from any other
 * source get the exception simple name as error key. Enabled in servlet web applications that have
 * {@code spring-tx} on the classpath, when {@code problem.dao-advice-enabled} is {@code true} or
 * unset.
 *
 * <p>Has default precedence, so an application's own advice for the same exceptions wins. Ordered
 * before {@link WebExceptionHandler}, whose {@code Throwable} fallback would otherwise catch these
 * exceptions first.
 */
@AutoConfiguration(before = WebExceptionHandler.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DataIntegrityViolationException.class)
@ConditionalOnProperty(
    prefix = "problem",
    name = "dao-advice-enabled",
    havingValue = "true",
    matchIfMissing = true)
@RestControllerAdvice
public class WebDaoExceptionHandler implements DaoAdvice {

  /** Resolvers consulted in order; the first that supports the cause is used. */
  protected final List<ConstraintNameResolver> constraintNameResolvers;

  /** Base URL for problem types, from {@code problem.type-url}, or {@code null} when unset. */
  private final @Nullable URI typeUrl;

  /**
   * Creates a handler that consults the given constraint-name resolvers.
   *
   * @param constraintNameResolvers the resolvers to consult, possibly empty
   * @param typeUrl the {@code problem.type-url} property, may be blank
   */
  protected WebDaoExceptionHandler(
      final List<ConstraintNameResolver> constraintNameResolvers,
      @Value("${problem.type-url:}") final String typeUrl) {
    this.constraintNameResolvers = List.copyOf(constraintNameResolvers);
    this.typeUrl = Exceptional.parseTypeUrl(typeUrl);
  }

  /** {@inheritDoc} */
  @Override
  public @Nullable URI typeUrl() {
    return this.typeUrl;
  }

  /** {@inheritDoc} */
  @Override
  public @Nullable String resolveConstraintName(final Throwable cause) {
    return this.constraintNameResolvers.stream()
        .filter(resolver -> resolver.supports(cause))
        .findFirst()
        .map(resolver -> resolver.resolveConstraintName(cause))
        .orElse(null);
  }
}
