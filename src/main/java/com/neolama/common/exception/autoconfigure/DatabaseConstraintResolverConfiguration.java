package com.neolama.common.exception.autoconfigure;

import com.mongodb.MongoClientSettings;
import com.neolama.common.exception.advice.dao.constraint.ConstraintNameResolver;
import com.neolama.common.exception.advice.dao.constraint.MongoConstraintNameResolver;
import com.neolama.common.exception.advice.dao.constraint.PostgresConstraintNameResolver;
import org.postgresql.PGConnection;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Registers database-specific {@link ConstraintNameResolver} beans when the matching driver is
 * present.
 *
 * <p>Active in servlet web applications that have {@code spring-tx} on the classpath, when {@code
 * problem.dao-advice-enabled} is {@code true} or unset.
 */
@AutoConfiguration(before = WebDaoExceptionHandler.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DataIntegrityViolationException.class)
@ConditionalOnProperty(
    prefix = "problem",
    name = "dao-advice-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class DatabaseConstraintResolverConfiguration {

  /** Configuration for PostgreSQL constraint name resolution. */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(PGConnection.class)
  static class PostgresqlConstraintNameResolverConfiguration {

    /**
     * Creates a {@link PostgresConstraintNameResolver} bean.
     *
     * @return the PostgreSQL constraint name resolver
     */
    @Bean
    ConstraintNameResolver postgresqlConstraintNameResolver() {
      return new PostgresConstraintNameResolver();
    }
  }

  /** Configuration for MongoDB constraint name resolution. */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(MongoClientSettings.class)
  static class MongoConstraintNameResolverConfiguration {

    /**
     * Creates a {@link MongoConstraintNameResolver} bean.
     *
     * @return the MongoDB constraint name resolver
     */
    @Bean
    ConstraintNameResolver mongoConstraintNameResolver() {
      return new MongoConstraintNameResolver();
    }
  }
}
