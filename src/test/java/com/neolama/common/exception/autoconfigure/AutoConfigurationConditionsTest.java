package com.neolama.common.exception.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.neolama.common.exception.dao.ConcurrencyFailureDetector;
import com.neolama.common.exception.dao.constraint.ConstraintNameResolver;
import com.neolama.common.exception.dao.constraint.MongoConstraintNameResolver;
import com.neolama.common.exception.dao.constraint.PostgresConstraintNameResolver;
import com.neolama.common.exception.jackson.ProblemModule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.postgresql.PGConnection;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

/** Checks which handlers each kind of application gets. */
class AutoConfigurationConditionsTest {

  private static final AutoConfigurations HANDLERS =
      AutoConfigurations.of(
          ProblemConfiguration.class,
          DataAccessConfiguration.class,
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
          assertThat(context).hasSingleBean(ConcurrencyFailureDetector.class);
          assertThat(context).hasSingleBean(ProblemMessageProvider.class);
          assertThat(context).hasSingleBean(ProblemModule.class);
        });
  }

  @Test
  void servletAppWithoutSpringTxSkipsDaoHandler() {
    this.servletRunner
        .withClassLoader(new FilteredClassLoader("org.springframework.dao"))
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(WebExceptionHandler.class);
              assertThat(context).doesNotHaveBean(WebDaoExceptionHandler.class);
              assertThat(context).doesNotHaveBean(ConstraintNameResolver.class);
              assertThat(context).doesNotHaveBean(ConcurrencyFailureDetector.class);
            });
  }

  /** Scheduled jobs and listeners can ask the detector without a web layer. */
  @Test
  void nonWebAppGetsDataAccessSupport() {
    new ApplicationContextRunner()
        .withConfiguration(HANDLERS)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(ConcurrencyFailureDetector.class);
              assertThat(context).getBeans(ConstraintNameResolver.class).hasSize(2);
              assertThat(context)
                  .getBean(ConcurrencyFailureDetector.class)
                  .satisfies(
                      detector ->
                          assertThat(
                                  detector.isConcurrencyFailure(
                                      new MongoException(112, "WriteConflict")))
                              .isTrue());
            });
  }

  /** The detector needs only {@code spring-tx}, not Spring Web or Spring MVC. */
  @Test
  void dataAccessSupportNeedsNoSpringWeb() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(DataAccessConfiguration.class))
        .withClassLoader(new FilteredClassLoader("org.springframework.web"))
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(ConcurrencyFailureDetector.class);
            });
  }

  @Test
  void reactiveAppGetsDataAccessSupport() {
    new ReactiveWebApplicationContextRunner()
        .withConfiguration(HANDLERS)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(ConcurrencyFailureDetector.class);
              assertThat(context).getBeans(ConstraintNameResolver.class).hasSize(2);
            });
  }

  @Test
  void applicationDetectorReplacesDefault() {
    final ConcurrencyFailureDetector own = new ConcurrencyFailureDetector(List.of());
    this.servletRunner
        .withBean(ConcurrencyFailureDetector.class, () -> own)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).getBean(ConcurrencyFailureDetector.class).isSameAs(own);
              assertThat(context)
                  .getBean(WebDaoExceptionHandler.class)
                  .extracting("concurrencyFailureDetector")
                  .isSameAs(own);
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
              assertThat(context).getBeans(ConstraintNameResolver.class).hasSize(2);
              assertThat(context).hasSingleBean(ConcurrencyFailureDetector.class);
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
