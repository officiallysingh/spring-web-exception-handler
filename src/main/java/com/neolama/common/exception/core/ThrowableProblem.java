package com.neolama.common.exception.core;

import java.util.Map;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

@Getter
public class ThrowableProblem extends RuntimeException {

  private final ProblemDetails problemDetails;

  private ThrowableProblem(final Throwable cause, final ProblemDetails problemDetails) {
    super(problemDetails.getDetail(), cause);
    this.problemDetails = problemDetails;
  }

  private ThrowableProblem(final ProblemDetails problemDetails) {
    super(problemDetails.getDetail());
    this.problemDetails = problemDetails;
  }

  public static ThrowableProblem of(final ProblemDetails problemDetail) {
    Assert.notNull(problemDetail, "'problemDetails' must not be null");
    return new ThrowableProblem(problemDetail);
  }

  public static ThrowableProblem of(
      final ProblemDetails problemDetail, @Nullable final Throwable cause) {
    Assert.notNull(problemDetail, "'problemDetails' must not be null");
    return new ThrowableProblem(cause, problemDetail);
  }

  /**
   * Compact line used by the logger. Unset fields are left out, so a missing file logs as {@code
   * ThrowableProblem: GET /api/v1/blobs/69285fc89b0e1b8db3e753a3 — 404 file-not-found: File not
   * found}.
   */
  @Override
  public String toString() {
    final StringBuilder message = new StringBuilder(128);
    message.append(getClass().getSimpleName()).append(": ");
    final int requestStart = message.length();
    append(message, "", this.problemDetails.getMethod());
    append(message, message.length() > requestStart ? " " : "", this.problemDetails.getInstance());
    if (message.length() > requestStart) {
      message.append(" — ");
    }
    message.append(this.problemDetails.getStatus());
    append(message, " ", this.problemDetails.getCode());
    append(message, ": ", this.problemDetails.getDetail());
    final Map<String, Object> properties = this.problemDetails.getProperties();
    if (properties != null && !properties.isEmpty()) {
      message.append(' ').append(properties);
    }
    return message.toString();
  }

  private static void append(
      final StringBuilder message, final String prefix, @Nullable final Object value) {
    if (value == null) {
      return;
    }
    final String text = value.toString();
    if (text.isBlank()) {
      return;
    }
    message.append(prefix).append(text);
  }
}
