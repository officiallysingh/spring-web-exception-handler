package com.neolama.common.exception.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.advice.security.ProblemAccessDeniedHandler;
import com.neolama.common.exception.advice.security.ProblemAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

/** The library's security beans back off when the application defines its own. */
class SecurityBeansTest {

  private static final AuthenticationEntryPoint USER_ENTRY_POINT =
      (request, response, exception) -> {};

  private static final AccessDeniedHandler USER_ACCESS_DENIED_HANDLER =
      (request, response, exception) -> {};

  private final WebApplicationContextRunner runner =
      new WebApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  ProblemConfiguration.class,
                  WebSecurityExceptionHandler.class,
                  WebExceptionHandler.class))
          .withBean(
              "handlerExceptionResolver",
              HandlerExceptionResolver.class,
              () -> (request, response, handler, exception) -> null);

  @Test
  void libraryBeansAreCreatedByDefault() {
    this.runner.run(
        context -> {
          assertThat(context).hasNotFailed();
          assertThat(context)
              .getBean(AuthenticationEntryPoint.class)
              .isInstanceOf(ProblemAuthenticationEntryPoint.class);
          assertThat(context)
              .getBean(AccessDeniedHandler.class)
              .isInstanceOf(ProblemAccessDeniedHandler.class);
        });
  }

  @Test
  void userEntryPointReplacesLibraryOne() {
    this.runner
        .withBean("userEntryPoint", AuthenticationEntryPoint.class, () -> USER_ENTRY_POINT)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context)
                  .getBean(AuthenticationEntryPoint.class)
                  .isSameAs(USER_ENTRY_POINT);
              assertThat(context)
                  .getBean(AccessDeniedHandler.class)
                  .isInstanceOf(ProblemAccessDeniedHandler.class);
            });
  }

  @Test
  void userAccessDeniedHandlerReplacesLibraryOne() {
    this.runner
        .withBean(
            "userAccessDeniedHandler", AccessDeniedHandler.class, () -> USER_ACCESS_DENIED_HANDLER)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context)
                  .getBean(AccessDeniedHandler.class)
                  .isSameAs(USER_ACCESS_DENIED_HANDLER);
              assertThat(context)
                  .getBean(AuthenticationEntryPoint.class)
                  .isInstanceOf(ProblemAuthenticationEntryPoint.class);
            });
  }

  @Test
  void userBeanWithSameNameDoesNotFailStartup() {
    this.runner
        .withBean(
            "authenticationEntryPoint", AuthenticationEntryPoint.class, () -> USER_ENTRY_POINT)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context)
                  .getBean(AuthenticationEntryPoint.class)
                  .isSameAs(USER_ENTRY_POINT);
            });
  }
}
