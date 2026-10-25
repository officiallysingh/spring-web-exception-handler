package com.neolama.common.exception.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoClientSettings;
import com.neolama.common.exception.advice.dao.constraint.ConstraintNameResolver;
import com.neolama.common.exception.advice.dao.constraint.MongoConstraintNameResolver;
import com.neolama.common.exception.advice.dao.constraint.PostgresConstraintNameResolver;
import com.neolama.common.exception.jackson.ProblemModule;
import org.junit.jupiter.api.Test;
import org.postgresql.PGConnection;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

/** Checks which handlers each kind of application gets. */
class AutoConfigurationConditionsTest {

  private static final AutoConfigurations HANDLERS =
      AutoConfigurations.of(
          ProblemConfiguration.class,
          DatabaseConstraintResolverConfiguration.class,
          WebDaoExceptionHandler.class,
          WebSecurityExceptionHandler.class,
          WebExceptionHandler.class);

  private final WebApplicationContextRunner servletRunner =
      new WebApplicationContextRunner()
          .withConfiguration(HANDLERS)
          .withBean(
              "handlerExceptionResolver",
              HandlerExceptionResolver.class,
              () -> (request, response, handler, exception) -> null);

  @Test
  void servletAppWithSpringTxGetsAllHandlers() {
    this.servletRunner.run(
        context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).hasSingleBean(WebExceptionHandler.class);
          assertThat(context).hasSingleBean(WebDaoExceptionHandler.class);
          assertThat(context).hasSingleBean(WebSecurityExceptionHandler.class);
          assertThat(context).getBeans(ConstraintNameResolver.class).isNotEmpty();
          assertThat(context).hasSingleBean(ProblemMessageProvider.class);
          assertThat(context).hasSingleBean(ProblemModule.class);
        });
  }

  @Test
  void servletAppWithoutSpringTxSkipsDaoHandler() {
    this.servletRunner
        .withClassLoader(new FilteredClassLoader(DataIntegrityViolationException.class))
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(WebExceptionHandler.class);
              assertThat(context).doesNotHaveBean(WebDaoExceptionHandler.class);
              assertThat(context).doesNotHaveBean(ConstraintNameResolver.class);
            });
  }

  @Test
  void nonWebAppGetsNoHandlers() {
    new ApplicationContextRunner()
        .withConfiguration(HANDLERS)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(WebExceptionHandler.class);
              assertThat(context).doesNotHaveBean(WebDaoExceptionHandler.class);
              assertThat(context).doesNotHaveBean(WebSecurityExceptionHandler.class);
              assertThat(context).doesNotHaveBean(AuthenticationEntryPoint.class);
              assertThat(context).doesNotHaveBean(ProblemMessageProvider.class);
              assertThat(context).doesNotHaveBean(ProblemModule.class);
            });
  }

  @Test
  void reactiveAppGetsNoHandlers() {
    new ReactiveWebApplicationContextRunner()
        .withConfiguration(HANDLERS)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(WebExceptionHandler.class);
              assertThat(context).doesNotHaveBean(WebDaoExceptionHandler.class);
              assertThat(context).doesNotHaveBean(WebSecurityExceptionHandler.class);
              assertThat(context).doesNotHaveBean(AuthenticationEntryPoint.class);
              assertThat(context).doesNotHaveBean(ProblemMessageProvider.class);
              assertThat(context).doesNotHaveBean(ProblemModule.class);
            });
  }

  @Test
  void daoAdviceCanBeDisabled() {
    this.servletRunner
        .withPropertyValues("problem.dao-advice-enabled=false")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(WebDaoExceptionHandler.class);
              assertThat(context).doesNotHaveBean(ConstraintNameResolver.class);
              assertThat(context).hasSingleBean(WebExceptionHandler.class);
            });
  }

  @Test
  void securityAdviceCanBeDisabled() {
    this.servletRunner
        .withPropertyValues("problem.security-advice-enabled=false")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(WebSecurityExceptionHandler.class);
              assertThat(context).doesNotHaveBean(AuthenticationEntryPoint.class);
              assertThat(context).hasSingleBean(WebExceptionHandler.class);
            });
  }

  @Test
  void securityAdviceNeedsSpringSecurity() {
    this.servletRunner
        .withClassLoader(new FilteredClassLoader(SecurityAutoConfiguration.class))
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(WebSecurityExceptionHandler.class);
            });
  }

  @Test
  void eachResolverNeedsItsDriver() {
    this.servletRunner
        .withClassLoader(new FilteredClassLoader(PGConnection.class))
        .run(
            context ->
                assertThat(context)
                    .getBeans(ConstraintNameResolver.class)
                    .values()
                    .singleElement()
                    .isInstanceOf(MongoConstraintNameResolver.class));
    this.servletRunner
        .withClassLoader(new FilteredClassLoader(MongoClientSettings.class))
        .run(
            context ->
                assertThat(context)
                    .getBeans(ConstraintNameResolver.class)
                    .values()
                    .singleElement()
                    .isInstanceOf(PostgresConstraintNameResolver.class));
  }
}
