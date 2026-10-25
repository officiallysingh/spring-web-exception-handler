package com.neolama.common.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.sql.SQLException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/data-integrity-null-message")
    String dataIntegrityNullMessage() {
      throw new DataIntegrityViolationException("insert failed", new SQLException((String) null));
    }

    @GetMapping("/duplicate-key-postgres")
    String duplicateKeyPostgres() {
      throw new DuplicateKeyException(
          "insert failed",
          new PSQLException(
              "ERROR: duplicate key value violates unique constraint \"employees_name_key\"",
              PSQLState.UNIQUE_VIOLATION));
    }

    @GetMapping("/duplicate-key-postgres-unconfigured")
    String duplicateKeyPostgresUnconfigured() {
      throw new DuplicateKeyException(
          "insert failed",
          new PSQLException(
              "ERROR: duplicate key value violates unique constraint \"orders_ref_key\"\n"
                  + "  Detail: Key (ref)=(secret-ref) already exists.",
              PSQLState.UNIQUE_VIOLATION));
    }

    @GetMapping("/upload-file-size-tomcat")
    String uploadFileSizeTomcat() {
      throw new MaxUploadSizeExceededException(
          -1,
          new IllegalStateException(
              new FileSizeLimitExceededException(
                  "The field file1 exceeds its maximum permitted size of 1048576 bytes.",
                  2000000,
                  1048576)));
    }

    @GetMapping("/upload-request-size-tomcat")
    String uploadRequestSizeTomcat() {
      throw new MaxUploadSizeExceededException(
          -1,
          new IllegalStateException(
              new SizeLimitExceededException(
                  "the request was rejected because its size (2000000) exceeds the configured"
                      + " maximum (1048576)",
                  2000000,
                  1048576)));
    }

    @GetMapping("/status-reason")
    String statusReason() {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee 7 not found");
    }

    @GetMapping("/status-499")
    String status499() {
      throw new ResponseStatusException(HttpStatusCode.valueOf(499));
    }

    @GetMapping("/page")
    String page(@RequestParam @Min(0) final int page) {
      return "page " + page;
    }

    @GetMapping("/not-null-violation")
    String notNullViolation() {
      throw new DataIntegrityViolationException(
          "insert failed",
          new PSQLException(
              "ERROR: null value in column \"name\" of relation \"employees\" violates not-null"
                  + " constraint",
              PSQLState.NOT_NULL_VIOLATION));
    }

    @GetMapping("/illegal-state")
    String illegalState() {
      throw new IllegalStateException("db password is x");
    }

    @GetMapping("/username-not-found")
    String usernameNotFound() {
      throw new UsernameNotFoundException("User alice not found");
    }

    @PostMapping("/validated")
    String validated(@Valid @RequestBody final Payload payload) {
      return payload.name();
    }
  }

  /** Request body with one required field. */
  record Payload(@NotBlank String name) {}
}
