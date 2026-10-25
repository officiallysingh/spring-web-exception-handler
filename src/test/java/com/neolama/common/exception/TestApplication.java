package com.neolama.common.exception;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Servlet application used by the advice tests. Its controllers live in the {@code support}
 * package; each throws the exceptions of one advice area.
 */
@SpringBootApplication
public class TestApplication {

  /** Lets every request through so the advices, not Spring Security, decide the response. */
  @Bean
  SecurityFilterChain permitAll(final HttpSecurity http) {
    return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .build();
  }
}
