package com.neolama.apps.useradvice;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import java.sql.SQLException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Application with its own {@code @RestControllerAdvice}, in a package outside the library so the
 * other tests do not pick the advice up.
 */
@SpringBootApplication
public class UserAdviceTestApplication {

  /** Lets every request through so advices, not security, decide the response. */
  @Bean
  SecurityFilterChain permitAll(final HttpSecurity http) {
    return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .build();
  }

  /** Endpoints that throw the exceptions the user advice handles. */
  @RestController
  static class UserController {

    @GetMapping("/user/data-integrity")
    String dataIntegrity() {
      throw new DataIntegrityViolationException("insert failed", new SQLException("boom"));
    }

    @GetMapping("/user/access-denied")
    String accessDenied() {
      throw new AccessDeniedException("no");
    }

    @GetMapping("/user/illegal-state")
    String illegalState() {
      throw new IllegalStateException("boom");
    }

    @GetMapping("/user/payment-declined")
    String paymentDeclined() {
      throw new PaymentDeclinedException();
    }
  }

  /** An application exception handled by an application advice built on {@link Exceptional}. */
  static class PaymentDeclinedException extends RuntimeException {}

  /** Application advice that uses the library's helpers, as the README shows. */
  @RestControllerAdvice
  static class PaymentAdvice implements Exceptional {

    @ExceptionHandler
    ProblemDetails handlePaymentDeclined(
        final PaymentDeclinedException exception, final NativeWebRequest request) {
      final ProblemDetails problemDetails =
          toProblemDetails(
              "PaymentDeclinedException", HttpStatus.PAYMENT_REQUIRED, "Payment was declined");
      return toResponse(problemDetails, request, exception);
    }
  }

  /** Application advice with the default (lowest) precedence and a 418 marker response. */
  @RestControllerAdvice
  static class UserAdvice {

    @ExceptionHandler({
      DataIntegrityViolationException.class,
      AccessDeniedException.class,
      IllegalStateException.class
    })
    ProblemDetail handle(final Exception exception) {
      final ProblemDetail body = ProblemDetail.forStatus(HttpStatus.I_AM_A_TEAPOT);
      body.setDetail("handled by user advice: " + exception.getClass().getSimpleName());
      return body;
    }
  }
}
