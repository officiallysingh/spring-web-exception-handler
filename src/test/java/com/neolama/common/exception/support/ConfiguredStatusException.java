package com.neolama.common.exception.support;

/** Exception whose status is set by {@code status.ConfiguredStatusException} in the test bundle. */
public class ConfiguredStatusException extends RuntimeException {

  public ConfiguredStatusException(final String message) {
    super(message);
  }
}
