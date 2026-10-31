package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.neolama.common.exception.core.ProblemDetails;
import java.net.URI;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Status resolution, problem type rules, and the {@code problem.type-url} parser of {@link
 * Exceptional}.
 */
class ExceptionalTest {

  private static final URI BASE = URI.create("https://example.com/problems");

  private static Exceptional withTypeUrl(final @Nullable URI typeUrl) {
    return new Exceptional() {
      @Override
      public @Nullable URI typeUrl() {
        return typeUrl;
      }
    };
  }

  private static ProblemDetails problem(final String code) {
    return ProblemDetails.of(HttpStatus.NOT_FOUND, code, "Not Found", "detail");
  }

  /** A {@code null} key array, possible from code without NullAway, means "no keys". */
  @Test
  void nullErrorKeysFallBackToTheSimpleName() {
    final Exceptional exceptional = new Exceptional() {};
    final IllegalStateException exception = new IllegalStateException("x");
    assertThat(exceptional.resolveStatus(exception, HttpStatus.BAD_REQUEST, (String[]) null))
        .isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(exceptional.resolveStatus(exception, HttpStatus.BAD_REQUEST))
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void typeUrlIsUnsetByDefault() {
    assertThat(new Exceptional() {}.typeUrl()).isNull();
  }

  @Test
  void withoutTypeUrlTypeStaysAboutBlank() {
    assertThat(withTypeUrl(null).typeUri(problem("not-found")))
        .isEqualTo(URI.create("about:blank"));
  }

  @Test
  void typeIsUrlWithCodeAsFragment() {
    assertThat(withTypeUrl(BASE).typeUri(problem("not-found")))
        .isEqualTo(URI.create("https://example.com/problems#not-found"));
  }

  @Test
  void blankCodeKeepsType() {
    assertThat(withTypeUrl(BASE).typeUri(problem(" "))).isEqualTo(URI.create("about:blank"));
  }

  @Test
  void explicitTypeIsKept() {
    final ProblemDetails details = problem("not-found");
    details.setType(URI.create("https://other.example/custom"));
    assertThat(withTypeUrl(BASE).typeUri(details))
        .isEqualTo(URI.create("https://other.example/custom"));
  }

  @Test
  void parseTypeUrl() {
    assertThat(Exceptional.parseTypeUrl(null)).isNull();
    assertThat(Exceptional.parseTypeUrl("  ")).isNull();
    assertThat(Exceptional.parseTypeUrl(" https://example.com/problems ")).isEqualTo(BASE);
    assertThatIllegalArgumentException().isThrownBy(() -> Exceptional.parseTypeUrl("problems"));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> Exceptional.parseTypeUrl("http://exa mple.com"));
  }
}
