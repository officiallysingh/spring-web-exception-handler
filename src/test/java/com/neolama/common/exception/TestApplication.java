package com.neolama.common.exception;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Minimal application used by the advice integration tests. */
@SpringBootApplication
public class TestApplication {

  /** Lets every request through so advices, not security, decide the response. */
  @Bean
  SecurityFilterChain permitAll(final HttpSecurity http) {
    return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .build();
  }

  /** Endpoints that throw the exceptions under test. */
  @RestController
  static class TestController {

    @GetMapping("/async-timeout")
    String asyncTimeout() {
      throw new AsyncRequestTimeoutException();
    }

    @GetMapping("/data-buffer-limit")
    String dataBufferLimit() {
      throw new DataBufferLimitException("Part exceeded the disk usage limit of 1024 bytes");
    }

    @GetMapping("/max-upload-size")
    String maxUploadSize() {
      throw new MaxUploadSizeExceededException(1048576);
    }
  }
}
