package com.neolama.oxneer.common.exception.autoconfigure;

import com.neolama.oxneer.common.exception.jackson.ProblemModule;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class ProblemConfiguration {

  @Bean
  ProblemMessageProvider problemMessageProvider(final MessageSource messageSource) {
    return new ProblemMessageProvider(messageSource);
  }

  @Bean
  ProblemModule problemModule() {
    return new ProblemModule();
  }
}
