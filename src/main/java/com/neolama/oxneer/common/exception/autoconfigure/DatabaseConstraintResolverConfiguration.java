package com.neolama.oxneer.common.exception.autoconfigure;

import com.mongodb.MongoClientSettings;
import com.neolama.oxneer.common.exception.advice.dao.constraint.ConstraintNameResolver;
import com.neolama.oxneer.common.exception.advice.dao.constraint.MongoConstraintNameResolver;
import com.neolama.oxneer.common.exception.advice.dao.constraint.PostgresConstraintNameResolver;
import org.postgresql.PGConnection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
    prefix = "problem",
    name = "dao-advice-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class DatabaseConstraintResolverConfiguration {

  /** Configuration for PostgreSQL constraint name resolution. */
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
