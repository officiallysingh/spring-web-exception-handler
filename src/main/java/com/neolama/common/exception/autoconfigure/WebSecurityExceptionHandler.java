package com.neolama.common.exception.autoconfigure;

import com.neolama.common.exception.advice.security.ProblemAccessDeniedHandler;
import com.neolama.common.exception.advice.security.ProblemAuthenticationEntryPoint;
import com.neolama.common.exception.advice.security.SecurityAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Controller advice that turns Spring Security authentication and access failures into problem
 * details.
 *
 * <p>Also registers an {@link AuthenticationEntryPoint} and an {@link AccessDeniedHandler} that
 * delegate to the MVC exception resolver. Enabled when Spring Security is on the classpath and
 * {@code problem.security-advice-enabled} is {@code true} or unset.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "problem",
    name = "security-advice-enabled",
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnClass(value = {SecurityAutoConfiguration.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class WebSecurityExceptionHandler implements SecurityAdvice {

  /**
   * Creates an entry point that forwards authentication failures to the MVC exception resolver.
   *
   * @param resolver the MVC exception resolver
   * @return the authentication entry point
   */
  @Bean
  AuthenticationEntryPoint authenticationEntryPoint(
      @Qualifier("handlerExceptionResolver") final HandlerExceptionResolver resolver) {
    return new ProblemAuthenticationEntryPoint(resolver);
  }

  /**
   * Creates an access-denied handler that forwards authorization failures to the MVC exception
   * resolver.
   *
   * @param resolver the MVC exception resolver
   * @return the access-denied handler
   */
  @Bean
  AccessDeniedHandler accessDeniedHandler(
      @Qualifier("handlerExceptionResolver") final HandlerExceptionResolver resolver) {
    return new ProblemAccessDeniedHandler(resolver);
  }
}
