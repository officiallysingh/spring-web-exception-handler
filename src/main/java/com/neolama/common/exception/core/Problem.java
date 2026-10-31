package com.neolama.common.exception.core;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.*;
import org.jspecify.annotations.Nullable;

/**
 * A single problem entry with a code, message, and detail text.
 *
 * <p>Factory methods that take an error key resolve those fields from the configured message
 * source.
 */
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"code", "message", "details"})
public class Problem implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** Stable code that identifies this problem. */
  private String code;

  /** Short message describing this problem. */
  private String message;

  /** Longer explanation of this problem. */
  private String details;

  /**
   * Creates a problem from explicit code, message, and detail values.
   *
   * @param code the problem code
   * @param message the problem message
   * @param details the problem detail
   * @return a new problem
   */
  public static Problem of(String code, String message, String details) {
    return new Problem(code, message, details);
  }

  /**
   * Creates a problem by resolving code, message, and detail for {@code errorKey}.
   *
   * <p>Missing messages fall back to the supplied defaults, or to the library defaults when a
   * supplied value is {@code null}: the error key in kebab-case as code, such as {@code
   * order-not-found} for {@code order.not.found}, {@value ProblemConstant#DEFAULT_TITLE} as
   * message, and {@value ProblemConstant#DEFAULT_PROBLEM_DETAILS} as details.
   *
   * @param errorKey the message-source key used to resolve the problem text
   * @param defaultCode the code used when none is configured, or {@code null} for the library
   *     default
   * @param defaultMessage the message used when none is configured, or {@code null} for the library
   *     default
   * @param defaultDetails the detail used when none is configured, or {@code null} for the library
   *     default
   * @return a new problem
   * @throws IllegalArgumentException if the error key is not in the format of {@link
   *     ProblemUtils#requireValidErrorKey(String)}
   */
  public static Problem of(
      String errorKey,
      @Nullable String defaultCode,
      @Nullable String defaultMessage,
      @Nullable String defaultDetails) {
    ProblemUtils.requireValidErrorKey(errorKey);
    String code =
        ProblemMessageProvider.getMessage(
            CODE_CODE_PREFIX + errorKey,
            Optional.ofNullable(defaultCode)
                .orElseGet(() -> ProblemUtils.getProblemCode(errorKey)));
    String message =
        ProblemMessageProvider.getMessage(
            TITLE_CODE_PREFIX + errorKey,
            Optional.ofNullable(defaultMessage).orElse(DEFAULT_TITLE));
    String details =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey,
            Optional.ofNullable(defaultDetails).orElse(DEFAULT_PROBLEM_DETAILS));
    return of(code, message, details);
  }

  /**
   * Creates a problem by resolving code, message, and detail for {@code errorKey}.
   *
   * <p>Missing messages fall back to the library defaults, as for {@link #of(String, String,
   * String, String)}.
   *
   * @param errorKey the message-source key used to resolve the problem text
   * @return a new problem
   * @throws IllegalArgumentException if the error key is not in the format of {@link
   *     ProblemUtils#requireValidErrorKey(String)}
   */
  public static Problem of(String errorKey) {
    return of(errorKey, null, null, null);
  }
}
