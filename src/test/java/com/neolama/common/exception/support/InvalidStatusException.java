package com.neolama.common.exception.support;

/** Exception whose {@code status.InvalidStatusException} entry is not a number. */
public class InvalidStatusException extends RuntimeException {

  public InvalidStatusException(final String message) {
    super(message);
  }
}
