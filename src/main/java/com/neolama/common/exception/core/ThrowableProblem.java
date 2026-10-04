package com.neolama.common.exception.core;

import java.util.Map;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

/**
 * Runtime exception that carries {@link ProblemDetails} for a failed request.
 *
 * <p>{@link #toString()} produces a single log line and omits unset request fields.
 */
@Getter
public class ThrowableProblem extends RuntimeException {

  /** Problem details carried by this exception. */
  private final ProblemDetails problemDetails;

  /**
   * Creates an exception with the given cause and problem details.
   *
   * @param cause the cause of this exception
   * @param problemDetails the problem details to carry
   */
  private ThrowableProblem(final Throwable cause, final ProblemDetails problemDetails) {
    super(problemDetails.getDetail(), cause);
    this.problemDetails = problemDetails;
  }

  /**
   * Creates an exception for the given problem details.
   *
   * @param problemDetails the problem details to carry
   */
  private ThrowableProblem(final ProblemDetails problemDetails) {
    super(problemDetails.getDetail());
    this.problemDetails = problemDetails;
  }

  /**
   * Creates an exception for the given problem details.
   *
   * @param problemDetail the problem details to carry
   * @return a new exception whose message is the problem detail
   */
  public static ThrowableProblem of(final ProblemDetails problemDetail) {
    Assert.notNull(problemDetail, "'problemDetails' must not be null");
    return new ThrowableProblem(problemDetail);
  }

  /**
   * Creates an exception for the given problem details and cause.
   *
   * @param problemDetail the problem details to carry
   * @param cause the cause of this exception, or {@code null} when there is none
   * @return a new exception whose message is the problem detail
   */
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

  /**
   * Appends a non-blank value, preceded by {@code prefix}.
   *
   * @param message the buffer to append to
   * @param prefix the text written before the value
   * @param value the value to append, ignored when {@code null} or blank
   */
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
