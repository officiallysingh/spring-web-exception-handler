package com.neolama.common.exception.autoconfigure;

import com.mongodb.MongoClientSettings;
import com.neolama.common.exception.dao.ConcurrencyFailureDetector;
import com.neolama.common.exception.dao.constraint.ConstraintNameResolver;
import com.neolama.common.exception.dao.constraint.MongoConstraintNameResolver;
import com.neolama.common.exception.dao.constraint.PostgresConstraintNameResolver;
import org.postgresql.PGConnection;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Registers the data-access support beans: a {@link ConstraintNameResolver} for each database
 * driver present, and the {@link ConcurrencyFailureDetector} that consults them.
 *
 * <p>Active in any application that has {@code spring-tx} on the classpath, web or not, so that
 * scheduled jobs, event listeners, and retry predicates can use the detector. Independent of {@code
 * problem.dao-advice-enabled}, which only controls {@link WebDaoExceptionHandler}.
 */
@AutoConfiguration(before = WebDaoExceptionHandler.class)
@ConditionalOnClass(ConcurrencyFailureException.class)
public class DataAccessConfiguration {

  /**
   * Creates the {@link ConcurrencyFailureDetector} bean unless the application defines its own.
   *
   * @param constraintNameResolvers the resolver beans, consulted in their order
   * @return the concurrency failure detector
   */
  @Bean
  @ConditionalOnMissingBean
  ConcurrencyFailureDetector concurrencyFailureDetector(
      final ObjectProvider<ConstraintNameResolver> constraintNameResolvers) {
    return new ConcurrencyFailureDetector(constraintNameResolvers.orderedStream().toList());
  }

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
