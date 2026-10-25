package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Problems can be built in a plain unit test, before any Spring context has started.
 *
 * <p>Surefire runs each test class in its own JVM here, so no context from another test class has
 * set the message source yet.
 */
class ProblemsWithoutSpringTest {

  @Test
  void errorKeyUsesJavaDefaults() {
    final ProblemDetails details = Problems.of("order.not.found").throwAble().getProblemDetails();
    assertThat(details.getStatus()).isEqualTo(500);
    assertThat(details.getCode()).isEqualTo("internal-server-error");
    assertThat(details.getTitle()).isEqualTo("Internal Server Error");
    assertThat(details.getDetail()).isEqualTo("An unexpected error occurred");
  }

  @Test
  void errorTypeUsesItsStatusAndDetail() {
    final ErrorType type =
        DefaultProblem.of("OrderNotFound", "Order {0} not found", HttpStatus.NOT_FOUND);
    final ProblemDetails details = Problems.of(type).detailArgs(42).throwAble().getProblemDetails();
    assertThat(details.getStatus()).isEqualTo(404);
    assertThat(details.getCode()).isEqualTo("order-not-found");
    assertThat(details.getTitle()).isEqualTo("Not Found");
    assertThat(details.getDetail()).isEqualTo("Order 42 not found");
  }

  @Test
  void problemOfErrorKeyUsesLibraryDefaults() {
    final Problem problem = Problem.of("order.not.found");
    assertThat(problem.getCode()).isEqualTo("500");
    assertThat(problem.getMessage()).isEqualTo("Internal Server Error");
    assertThat(problem.getDetails()).isEqualTo("An unexpected error occurred");
  }
}
