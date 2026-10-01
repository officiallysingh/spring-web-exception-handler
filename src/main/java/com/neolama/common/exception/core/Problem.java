package com.neolama.common.exception.core;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"code", "message", "details"})
public class Problem implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private String code;

  private String message;

  private String details;

  public static Problem of(String code, String message, String details) {
    return new Problem(code, message, details);
  }

  public static Problem of(
      String errorKey,
      @Nullable String defaultCode,
      @Nullable String defaultMessage,
      @Nullable String defaultDetails) {
    Assert.hasText(errorKey, "'errorKey' must not be null or empty");
    String code =
        ProblemMessageProvider.getMessage(
            CODE_CODE_PREFIX + errorKey, Optional.ofNullable(defaultCode).orElse(DEFAULT_CODE));
    String message =
        ProblemMessageProvider.getMessage(
            TITLE_CODE_PREFIX + errorKey,
            Optional.ofNullable(defaultMessage).orElse(DEFAULT_TITLE));
    String details =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey,
            Optional.ofNullable(defaultDetails).orElse(DEFAULT_DETAIL));
    return of(code, message, details);
  }

  public static Problem of(String errorKey) {
    Assert.hasText(errorKey, "'errorKey' must not be null or empty");
    String code = ProblemMessageProvider.getMessage(CODE_CODE_PREFIX + errorKey, DEFAULT_CODE);
    String message = ProblemMessageProvider.getMessage(TITLE_CODE_PREFIX + errorKey, DEFAULT_TITLE);
    String details =
        ProblemMessageProvider.getMessage(DETAIL_CODE_PREFIX + errorKey, DEFAULT_DETAIL);
    return of(code, message, details);
  }
}
