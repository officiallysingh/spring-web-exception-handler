package com.neolama.common.exception.core;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * RFC 9457 problem details extended with a code, HTTP method, timestamp, and nested problems.
 *
 * <p>Code, title, and detail are resolved from the configured message source when an error key is
 * supplied. The problem type defaults to {@code about:blank}.
 */
@JsonPropertyOrder({
  "type",
  "status",
  "instance",
  METHOD_KEY,
  CODE_KEY,
  "title",
  "detail",
  TIMESTAMP_KEY,
  VIOLATIONS_KEY,
  ERRORS_KEY
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProblemDetails extends ProblemDetail {

  /** Origin reserved for problem type URIs. */
  private static final String TYPE_URL = "http://localhost:8090/";

  /** Stable code that identifies this problem. */
  private String code;

  /** HTTP method of the request that produced this problem. */
  @Setter private HttpMethod method;

  /** UTC time at which this problem was created. */
  private OffsetDateTime timestamp;

  /** Field and property violations associated with this problem. */
  private List<Problem> violations;

  /** Nested problems associated with this problem. */
  private List<Problem> errors;

  /**
   * Creates problem details for an error key and status.
   *
   * @param errorKey the message-source key used to resolve code, title, and detail
   * @param defaultDetail the detail used when no message is configured, or {@code null} for the
   *     library default
   * @param status the HTTP status of the problem
   * @return a new problem details instance
   */
  public static ProblemDetails of(
      final String errorKey, @Nullable final String defaultDetail, final HttpStatus status) {
    return of(errorKey, defaultDetail, null, status);
  }

  /**
   * Creates problem details for an error key, status, and detail arguments.
   *
   * @param errorKey the message-source key used to resolve code, title, and detail
   * @param defaultDetail the detail used when no message is configured, or {@code null} for the
   *     library default
   * @param detailArgs arguments substituted into the detail message, or {@code null} when the
   *     detail has none
   * @param status the HTTP status of the problem
   * @return a new problem details instance
   */
  public static ProblemDetails of(
      final String errorKey,
      @Nullable final String defaultDetail,
      @Nullable Object[] detailArgs,
      final HttpStatus status) {
    final String defaultCode =
        errorKey.contains(".")
            ? ProblemUtils.getProblemCode(status)
            : ProblemUtils.getProblemCode(errorKey);
    final String code = ProblemMessageProvider.getMessage(CODE_CODE_PREFIX + errorKey, defaultCode);
    final String title =
        ProblemMessageProvider.getMessage(TITLE_CODE_PREFIX + errorKey, status.getReasonPhrase());
    final String detail =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey,
            StringUtils.isNotBlank(defaultDetail) ? defaultDetail : DEFAULT_DETAIL,
            detailArgs);
    return of(status, code, title, detail);
  }

  /**
   * Creates problem details whose code, title, and detail are taken from the HTTP status.
   *
   * @param status the HTTP status of the problem
   * @return a new problem details instance
   */
  public static ProblemDetails of(final HttpStatus status) {
    return of(
        status, String.valueOf(status.value()), status.getReasonPhrase(), status.getReasonPhrase());
  }

  /**
   * Creates problem details from explicit status, code, title, and detail values.
   *
   * <p>The timestamp is the current UTC time and the type is {@code about:blank}.
   *
   * @param status the HTTP status of the problem
   * @param code the problem code
   * @param title the problem title
   * @param detail the problem detail
   * @return a new problem details instance
   */
  public static ProblemDetails of(
      final HttpStatus status, final String code, final String title, final String detail) {
    ProblemDetails problemDetails = new ProblemDetails();
    problemDetails.setType(DEFAULT_TYPE_URL);
    problemDetails.setStatus(status);
    problemDetails.setTitle(title);
    problemDetails.setDetail(detail);
    problemDetails.code = code;
    problemDetails.timestamp = OffsetDateTime.now(ZoneId.of("UTC"));
    return problemDetails;
  }

  /**
   * Replaces the violations with a copy of the given collection.
   *
   * @param violations the violations to store
   */
  public void setViolations(final Collection<Problem> violations) {
    this.violations = violations.stream().toList();
  }

  /**
   * Replaces the nested errors with a copy of the given collection.
   *
   * @param errors the nested problems to store
   */
  public void setErrors(final Collection<Problem> errors) {
    this.errors = errors.stream().toList();
  }
}
