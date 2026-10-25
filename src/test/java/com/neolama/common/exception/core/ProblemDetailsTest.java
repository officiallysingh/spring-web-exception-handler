package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.autoconfigure.TestMessageSources;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** {@link ProblemDetails} factories: code, title, detail, type, and timestamp. */
class ProblemDetailsTest {

  @AfterEach
  void resetMessages() {
    TestMessageSources.reset();
  }

  @Test
  void ofStatusUsesStatusForEverything() {
    final ProblemDetails details = ProblemDetails.of(HttpStatus.NOT_FOUND);
    assertThat(details.getStatus()).isEqualTo(404);
    assertThat(details.getCode()).isEqualTo("404");
    assertThat(details.getTitle()).isEqualTo("Not Found");
    assertThat(details.getDetail()).isEqualTo("Not Found");
    assertThat(details.getType()).isEqualTo(URI.create("about:blank"));
  }

  @Test
  void timestampIsUtc() {
    assertThat(ProblemDetails.of(HttpStatus.NOT_FOUND).getTimestamp().getOffset())
        .isEqualTo(ZoneOffset.UTC);
  }

  @Test
  void errorKeyWithoutDotGivesKebabCaseCode() {
    final ProblemDetails details = ProblemDetails.of("OrderNotFound", null, HttpStatus.NOT_FOUND);
    assertThat(details.getCode()).isEqualTo("order-not-found");
    assertThat(details.getTitle()).isEqualTo("Not Found");
    assertThat(details.getDetail()).isEqualTo("An unexpected error occurred");
  }

  @Test
  void errorKeyWithDotGivesStatusCode() {
    assertThat(ProblemDetails.of("order.not.found", null, HttpStatus.NOT_FOUND).getCode())
        .isEqualTo("not-found");
  }

  @Test
  void defaultDetailIsFormattedWithArguments() {
    final ProblemDetails details =
        ProblemDetails.of(
            "OrderNotFound", "Order {0} not found", new Object[] {42}, HttpStatus.NOT_FOUND);
    assertThat(details.getDetail()).isEqualTo("Order 42 not found");
  }

  @Test
  void blankDefaultDetailFallsBackToLibraryDefault() {
    assertThat(ProblemDetails.of("OrderNotFound", " ", HttpStatus.NOT_FOUND).getDetail())
        .isEqualTo("An unexpected error occurred");
  }

  @Test
  void messagesWinOverDefaults() {
    TestMessageSources.install(
        Map.of(
            "code.OrderNotFound", "ORD-404",
            "title.OrderNotFound", "Order not found",
            "detail.OrderNotFound", "Order {0} does not exist"));
    final ProblemDetails details =
        ProblemDetails.of("OrderNotFound", "Java detail", new Object[] {42}, HttpStatus.NOT_FOUND);
    assertThat(details.getCode()).isEqualTo("ORD-404");
    assertThat(details.getTitle()).isEqualTo("Order not found");
    assertThat(details.getDetail()).isEqualTo("Order 42 does not exist");
  }

  @Test
  void violationsAndErrorsAreCopied() {
    final List<Problem> problems = new ArrayList<>(List.of(Problem.of("a", "A", "first")));
    final ProblemDetails details = ProblemDetails.of(HttpStatus.BAD_REQUEST);
    details.setViolations(problems);
    details.setErrors(problems);
    problems.add(Problem.of("b", "B", "second"));
    assertThat(details.getViolations()).hasSize(1);
    assertThat(details.getErrors()).hasSize(1);
  }
}
