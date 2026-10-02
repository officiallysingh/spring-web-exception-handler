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

@Configuration
@ConditionalOnProperty(
    prefix = "problem",
    name = "dao-advice-enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class WebDaoExceptionHandler implements DaoAdvice {

  protected final Map<ConstraintNameResolver.Type, ConstraintNameResolver> constraintNameResolvers;

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
