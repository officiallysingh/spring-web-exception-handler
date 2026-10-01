package com.neolama.oxneer.common.exception.core;

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
   * ThrowableProblem: 404 Not Found: File not found {code=404}}.
   */
  @Override
  public String toString() {
    final StringBuilder message = new StringBuilder(128);
    message.append(getClass().getSimpleName()).append(": ").append(problemDetails.getStatus());
    final String title = problemDetails.getTitle();
    append(message, " ", title);
    final String detail = problemDetails.getDetail();
    if (detail != null && !detail.isBlank() && !detail.equals(title)) {
      message.append(": ").append(detail);
    }
    append(message, " type=", problemDetails.getType());
    append(message, " instance=", problemDetails.getInstance());
    final Map<String, Object> properties = problemDetails.getProperties();
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
