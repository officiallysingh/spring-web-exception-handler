package com.neolama.common.exception.advice;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger and log formats used by the logging methods of {@link Exceptional}.
 *
 * <p>Kept out of the interface, whose fields would be public and inherited by every advice. The
 * logger keeps the name {@code com.neolama.common.exception.advice.Exceptional}.
 */
@UtilityClass
class ExceptionalLog {

  /** Shared logger for advice traits. */
  final Logger LOGGER = LoggerFactory.getLogger(Exceptional.class);

  /** Banner written before a logged exception key. */
  final String EXCEPTION_KEY_LOG_START =
      "---------------------- Exception Key ----------------------";

  /** Banner written before logged error keys. */
  final String ERROR_KEYS_LOG_START = "---------------------- Error Keys ----------------------";

  /** Banner written after logged exception or error keys. */
  final String KEYS_LOG_END = "--------------------------------------------------------";

  /** Format of a logged key block: start banner, line break, keys, line break, end banner. */
  final String KEYS_LOG_FORMAT = "{}{}{}{}{}{}";

  /** Format of a logged problem: method, path, status, code, exception name, exception message. */
  final String PROBLEM_LOG_FORMAT = "{} {} — {} {}: {}: {}";
}
