package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.neolama.common.exception.autoconfigure.TestMessageSources;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** {@link Problem}: explicit values, and values resolved from the message source. */
class ProblemTest {

  @AfterEach
  void resetMessages() {
    TestMessageSources.reset();
  }

  @Test
  void ofExplicitValues() {
    final Problem problem = Problem.of("a-1", "First", "first detail");
    assertThat(problem.getCode()).isEqualTo("a-1");
    assertThat(problem.getMessage()).isEqualTo("First");
    assertThat(problem.getDetails()).isEqualTo("first detail");
  }

  @Test
  void ofErrorKeyUsesLibraryDefaults() {
    final Problem problem = Problem.of("order.not.found");
    assertThat(problem.getCode()).isEqualTo("order-not-found");
    assertThat(problem.getMessage()).isEqualTo("Internal Server Error");
    assertThat(problem.getDetails()).isEqualTo("Something went wrong while processing the request");
  }

  @Test
  void ofErrorKeyReadsMessages() {
    TestMessageSources.install(
        Map.of(
            "code.order.not.found", "ORD-404",
            "title.order.not.found", "Order not found",
            "detail.order.not.found", "No such order"));
    final Problem problem = Problem.of("order.not.found");
    assertThat(problem.getCode()).isEqualTo("ORD-404");
    assertThat(problem.getMessage()).isEqualTo("Order not found");
    assertThat(problem.getDetails()).isEqualTo("No such order");
  }

  @Test
  void ofErrorKeyWithDefaultsUsesThemWhenMessagesAreMissing() {
    final Problem problem = Problem.of("order.not.found", "ORD-404", "Order not found", "Gone");
    assertThat(problem.getCode()).isEqualTo("ORD-404");
    assertThat(problem.getMessage()).isEqualTo("Order not found");
    assertThat(problem.getDetails()).isEqualTo("Gone");
  }

  @Test
  void ofErrorKeyWithNullDefaultsUsesLibraryDefaults() {
    final Problem problem = Problem.of("order.not.found", null, null, null);
    assertThat(problem.getCode()).isEqualTo("order-not-found");
    assertThat(problem.getMessage()).isEqualTo("Internal Server Error");
    assertThat(problem.getDetails()).isEqualTo("Something went wrong while processing the request");
  }

  @Test
  void ofErrorKeyWithDefaultsPrefersMessages() {
    TestMessageSources.install(Map.of("detail.order.not.found", "From messages"));
    assertThat(Problem.of("order.not.found", null, null, "From Java").getDetails())
        .isEqualTo("From messages");
  }

  @Test
  void malformedErrorKeyIsRejected() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> Problem.of("12345"))
        .withMessageContaining("Invalid error key '12345'");
    assertThatIllegalArgumentException()
        .isThrownBy(() -> Problem.of("row 3", null, null, null))
        .withMessageContaining("Invalid error key 'row 3'");
  }

  @Test
  void blankErrorKeyIsRejected() {
    assertThatIllegalArgumentException().isThrownBy(() -> Problem.of(" "));
    assertThatIllegalArgumentException().isThrownBy(() -> Problem.of("", null, null, null));
  }

  @Test
  void equalProblemsAreEqual() {
    assertThat(Problem.of("a", "b", "c"))
        .isEqualTo(Problem.of("a", "b", "c"))
        .hasSameHashCodeAs(Problem.of("a", "b", "c"));
  }
}
