package com.neolama.common.exception.core;

import java.net.URI;
import lombok.experimental.UtilityClass;

/** Message-source prefixes, JSON property names, and fallback problem text. */
@UtilityClass
public class ProblemConstant {

  /** Problem type used when no more specific type URI is set. */
  public static final URI DEFAULT_TYPE_URL = URI.create("about:blank");

  /** JSON property name for the problem code. */
  public static final String CODE_KEY = "code";

  /** JSON property name for the HTTP method. */
  public static final String METHOD_KEY = "method";

  /** JSON property name for the problem timestamp. */
  public static final String TIMESTAMP_KEY = "timestamp";

  /** JSON property name for nested problems. */
  public static final String ERRORS_KEY = "errors";

  /** JSON property name for constraint and binding violations. */
  public static final String VIOLATIONS_KEY = "violations";

  /** Separator between segments of a message-source key. */
  public static final String DOT = ".";

  /** Message-source prefix for a problem code. */
  public static final String CODE_CODE_PREFIX = "code.";

  /** Message-source prefix for a problem title. */
  public static final String TITLE_CODE_PREFIX = "title.";

  /** Message-source prefix for a problem detail. */
  public static final String DETAIL_CODE_PREFIX = "detail.";

  /** Message-source prefix for an HTTP status code. */
  public static final String STATUS_CODE_PREFIX = "status.";

  /** Message-source prefix for a violation message. */
  public static final String MESSAGE_CODE_PREFIX = "message.";

  /** Fallback problem code when none is configured. */
  public static final String DEFAULT_CODE = "500";

  /** Fallback problem title when none is configured. */
  public static final String DEFAULT_TITLE = "Internal Server Error";

  /** Fallback problem detail when none is configured. */
  public static final String DEFAULT_DETAIL = "An unexpected error occurred";

  /** Fallback HTTP status code when none is configured. */
  public static final String DEFAULT_STATUS = "500";
}
