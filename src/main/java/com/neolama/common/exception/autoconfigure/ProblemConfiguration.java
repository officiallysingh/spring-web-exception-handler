package com.neolama.common.exception.autoconfigure;

import com.neolama.common.exception.jackson.ProblemModule;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Registers the problem message source and the Jackson module used to read and write problem
 * details.
 */
@Configuration
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class ProblemConfiguration {

  /**
   * Creates the provider that resolves problem text from the application message source.
   *
   * @param messageSource the message source used for lookups
   * @return the problem message provider
   */
  @Bean
  ProblemMessageProvider problemMessageProvider(final MessageSource messageSource) {
    return new ProblemMessageProvider(messageSource);
  }

  /**
   * Creates the Jackson module that serializes HTTP status codes and methods on problem details.
   *
   * @return the problem Jackson module
   */
  @Bean
  ProblemModule problemModule() {
    return new ProblemModule();
  }
}
