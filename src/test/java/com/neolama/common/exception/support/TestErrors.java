package com.neolama.common.exception.support;

import com.neolama.common.exception.core.ErrorType;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

/** Error types as an application would declare them, as recommended by {@link ErrorType}. */
public enum TestErrors implements ErrorType {
  ORDER_NOT_FOUND("OrderNotFound", "Order {0} not found", HttpStatus.NOT_FOUND),
  NO_STATUS("NoStatus", "Something went wrong", null),
  CONFIGURED("configured.error", "Java detail", HttpStatus.CONFLICT);

  private final String errorKey;
  private final String defaultDetail;
  private final @Nullable HttpStatus status;

  TestErrors(final String errorKey, final String defaultDetail, final @Nullable HttpStatus status) {
    this.errorKey = errorKey;
    this.defaultDetail = defaultDetail;
    this.status = status;
  }

  @Override
  public String getErrorKey() {
    return this.errorKey;
  }

  @Override
  public String getDefaultDetail() {
    return this.defaultDetail;
  }

  @Override
  public @Nullable HttpStatus getStatus() {
    return this.status;
  }
}
