package com.neolama.common.exception.autoconfigure;

import com.neolama.common.exception.advice.dao.DaoAdvice;
import com.neolama.common.exception.advice.dao.constraint.ConstraintNameResolver;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice that turns data-integrity and duplicate-key failures into problem details.
 *
 * <p>Selects a {@link ConstraintNameResolver} from the exception message. Enabled when {@code
 * problem.dao-advice-enabled} is {@code true} or unset.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "problem",
    name = "dao-advice-enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class WebDaoExceptionHandler implements DaoAdvice {

  /** Resolvers indexed by the database they support. */
  protected final Map<ConstraintNameResolver.Type, ConstraintNameResolver> constraintNameResolvers;

  /**
   * Creates a handler that indexes the given constraint-name resolvers by database type.
   *
   * @param constraintNameResolvers the resolvers to consult, possibly empty
   */
  protected WebDaoExceptionHandler(final List<ConstraintNameResolver> constraintNameResolvers) {
    if (CollectionUtils.isNotEmpty(constraintNameResolvers)) {
      this.constraintNameResolvers =
          constraintNameResolvers.stream()
              .collect(Collectors.toMap(ConstraintNameResolver::getType, Function.identity()));
    } else {
      this.constraintNameResolvers = Collections.emptyMap();
    }
  }

  /** {@inheritDoc} */
  @Override
  public String resolveConstraintName(final String exceptionMessage) {
    if (exceptionMessage.contains("WriteError")) { // MongoDB constraint violation
      return this.constraintNameResolvers
          .get(ConstraintNameResolver.Type.MONGO_DB)
          .resolveConstraintName(exceptionMessage);
    } else {
      return this.constraintNameResolvers
          .get(ConstraintNameResolver.Type.POSTGRESQL)
          .resolveConstraintName(exceptionMessage);
    }
  }
}
