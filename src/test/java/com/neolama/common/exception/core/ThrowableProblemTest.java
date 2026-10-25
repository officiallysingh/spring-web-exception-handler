package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/** {@link ThrowableProblem}: message, cause, and the compact log line. */
class ThrowableProblemTest {

  private static ProblemDetails details() {
    return ProblemDetails.of(HttpStatus.NOT_FOUND, "file-not-found", "Not Found", "File not found");
  }

  @Test
  void messageIsTheDetail() {
    assertThat(ThrowableProblem.of(details())).hasMessage("File not found").hasNoCause();
  }

  @Test
  void keepsCause() {
    final IllegalStateException cause = new IllegalStateException("boom");
    assertThat(ThrowableProblem.of(details(), cause)).hasCause(cause);
  }

  @Test
  void nullDetailsAreRejected() {
    assertThatIllegalArgumentException().isThrownBy(() -> ThrowableProblem.of(null));
    assertThatIllegalArgumentException().isThrownBy(() -> ThrowableProblem.of(null, null));
  }

  @Test
  void toStringWithoutRequestLeavesRequestOut() {
    assertThat(ThrowableProblem.of(details()))
        .hasToString("ThrowableProblem: 404 file-not-found: File not found");
  }

  @Test
  void toStringWithRequestAndProperties() {
    final ProblemDetails details = details();
    details.setMethod(HttpMethod.GET);
    details.setInstance(URI.create("/api/v1/blobs/42"));
    details.setProperties(Map.of("blobId", 42));
    assertThat(ThrowableProblem.of(details))
        .hasToString(
            "ThrowableProblem: GET /api/v1/blobs/42 — 404 file-not-found: File not found {blobId=42}");
  }

  @Test
  void toStringWithInstanceOnly() {
    final ProblemDetails details = details();
    details.setInstance(URI.create("/api/v1/blobs/42"));
    assertThat(ThrowableProblem.of(details))
        .hasToString("ThrowableProblem: /api/v1/blobs/42 — 404 file-not-found: File not found");
  }

  @Test
  void toStringLeavesOutBlankValues() {
    final ProblemDetails details =
        ProblemDetails.of(HttpStatus.NOT_FOUND, " ", "Not Found", "File not found");
    assertThat(ThrowableProblem.of(details)).hasToString("ThrowableProblem: 404: File not found");
  }
}
