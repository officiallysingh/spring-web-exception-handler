package com.neolama.oxneer.common.exception.autoconfigure;

import com.neolama.oxneer.common.exception.advice.security.ProblemAccessDeniedHandler;
import com.neolama.oxneer.common.exception.advice.security.ProblemAuthenticationEntryPoint;
import com.neolama.oxneer.common.exception.advice.security.SecurityAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@ConditionalOnClass(value = {SecurityAutoConfiguration.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class WebSecurityExceptionHandler implements SecurityAdvice {

  @Bean
  AuthenticationEntryPoint authenticationEntryPoint(
      @Qualifier("handlerExceptionResolver") final HandlerExceptionResolver resolver) {
    return new ProblemAuthenticationEntryPoint(resolver);
  }

  @Bean
  AccessDeniedHandler accessDeniedHandler(
      @Qualifier("handlerExceptionResolver") final HandlerExceptionResolver resolver) {
    return new ProblemAccessDeniedHandler(resolver);
  }
}
