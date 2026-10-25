package com.neolama.common.exception.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Application exception whose status comes from {@code @ResponseStatus}. */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

  public ConflictException(final String message) {
    super(message);
  }
}
