package com.neolama.common.exception.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Immutable {@link ErrorType} created with {@code DefaultProblem.of(errorKey, defaultDetail,
 * status)}.
 */
@Getter
@RequiredArgsConstructor(staticName = "of")
public class DefaultProblem implements ErrorType {

  /** Message-source key used to resolve externalized problem text. */
  private final String errorKey;

  /** Detail used when no message is configured for {@link #errorKey}. */
  private final String defaultDetail;

  /** HTTP status associated with this error type. */
  private final HttpStatus status;
}
