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

  /** Fallback details of a {@link Problem} entry when none are configured. */
  public static final String DEFAULT_PROBLEM_DETAILS =
      "Something went wrong while processing the request";

  /** Fallback problem detail when none is configured. */
  public static final String DEFAULT_DETAIL = "An unexpected error occurred";

  /** Client-safe fallback detail for a failed request validation. */
  public static final String VALIDATION_FAILED_DETAIL = "Request validation failed";

  /** Client-safe fallback detail for a duplicate-key or unique-constraint violation. */
  public static final String DUPLICATE_KEY_DETAIL = "The request conflicts with existing data";

  /** Fallback detail for an optimistic-lock conflict, such as an {@code @Version} mismatch. */
  public static final String OPTIMISTIC_LOCK_DETAIL =
      "The resource was changed by another request; reload it and try again";

  /** Fallback detail for a lock timeout, deadlock, or other temporary database contention. */
  public static final String CONCURRENCY_FAILURE_DETAIL =
      "The request could not be completed because of concurrent activity; please retry";

  /** Client-safe fallback detail for a request body that cannot be read. */
  public static final String MALFORMED_BODY_DETAIL = "Malformed request body";

  /** Client-safe fallback detail for an unknown user, so it reads like a wrong password. */
  public static final String BAD_CREDENTIALS_DETAIL = "Bad credentials";

  /** Fallback detail for an upload over the size limit; {@code {0}} is the readable limit. */
  public static final String UPLOAD_SIZE_EXCEEDED_DETAIL =
      "Upload file size exceeded the maximum allowed limit: {0}";

  /**
   * Fallback detail for downstream response data over a buffer limit; {@code {0}} is the readable
   * limit.
   */
  public static final String DATA_BUFFER_LIMIT_DETAIL =
      "Response data from a downstream service exceeded the allowed limit: {0}";
}
