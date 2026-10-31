package com.neolama.common.exception.advice;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.neolama.common.exception.autoconfigure.ProblemConfigProvider;
import com.neolama.common.exception.core.ProblemConstant;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.*;
import org.springframework.web.ErrorResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Base interface for all advice traits. Provides methods for resolving HTTP status, converting
 * throwables to problems, and building responses.
 */
public interface Exceptional {

  /**
   * Resolves the HTTP status for the given throwable.
   *
   * <p>Uses {@link HttpStatus#INTERNAL_SERVER_ERROR} when no more specific status can be resolved.
   *
   * @param throwable the throwable whose status is resolved
   * @return the resolved HTTP status
   */
  default HttpStatus resolveStatus(final Throwable throwable) {
    return resolveStatus(throwable, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Resolves the HTTP status for the given throwable. Every advice uses this one rule.
   *
   * <p>In order: a configured {@code status.{errorKey}} for the first of {@code errorKeys} that has
   * one, then {@code @ResponseStatus} on the throwable or one of its causes, then the status of a
   * Spring {@link ErrorResponse}, and finally {@code defaultStatus}.
   *
   * <p>Without {@code errorKeys}, the key is the throwable's {@linkplain
   * ProblemUtils#errorKey(Class) simple name}. Advices whose error key extends the exception name,
   * such as {@code MissingServletRequestParameterException.page}, pass that key first and the
   * exception name after it, so that a status can be set for one parameter or for all of them.
   *
   * @param throwable the throwable whose status is resolved
   * @param defaultStatus the status used when nothing else applies
   * @param errorKeys the error keys to look up, most specific first, or none or {@code null} for
   *     the simple name
   * @return the resolved HTTP status
   */
  default HttpStatus resolveStatus(
      final Throwable throwable,
      final HttpStatus defaultStatus,
      final String @Nullable ... errorKeys) {
    // A plain check rather than ArrayUtils.isEmpty, which NullAway cannot see through.
    final String[] keys =
        errorKeys == null || errorKeys.length == 0
            ? new String[] {ProblemUtils.errorKey(throwable.getClass())}
            : errorKeys;
    return ProblemUtils.configuredStatus(keys)
        .or(() -> ProblemUtils.resolveStatus(throwable))
        .or(() -> errorResponseStatus(throwable))
        .orElse(defaultStatus);
  }

  /**
   * Returns the status carried by a Spring {@link ErrorResponse}, such as {@code
   * NoResourceFoundException} or {@code AsyncRequestTimeoutException}.
   *
   * @param throwable the throwable to inspect
   * @return the status, or empty when the throwable is not an {@link ErrorResponse} or its code is
   *     not a known {@link HttpStatus}
   */
  private static Optional<HttpStatus> errorResponseStatus(final Throwable throwable) {
    return throwable instanceof ErrorResponse errorResponse
        ? Optional.ofNullable(HttpStatus.resolve(errorResponse.getStatusCode().value()))
        : Optional.empty();
  }

  /**
   * Converts the throwable into problem details, resolving the HTTP status from the throwable.
   *
   * @param throwable the throwable to convert
   * @return problem details for the throwable
   */
  default ProblemDetails toProblemDetails(final Throwable throwable) {
    final HttpStatus status = resolveStatus(throwable);
    return toProblemDetails(throwable, status);
  }

  /**
   * Converts the throwable into problem details with the given status.
   *
   * <p>For a server error the fallback detail is {@value ProblemConstant#DEFAULT_DETAIL}, so
   * internal messages such as SQL text, file paths, or class names stay in the server log. Other
   * statuses use the exception message. A {@code detail.{errorKey}} entry wins in both cases.
   *
   * @param throwable the throwable to convert
   * @param status the HTTP status of the problem
   * @return problem details for the throwable
   */
  default ProblemDetails toProblemDetails(final Throwable throwable, final HttpStatus status) {
    final @Nullable String defaultDetail =
        status.is5xxServerError() ? DEFAULT_DETAIL : throwable.getMessage();
    return toProblemDetails(throwable, status, defaultDetail);
  }

  /**
   * Converts the throwable into problem details with the given status and fallback detail.
   *
   * @param throwable the throwable to convert
   * @param status the HTTP status of the problem
   * @param defaultDetail the detail used when no message is configured for the throwable
   * @return problem details for the throwable
   */
  default ProblemDetails toProblemDetails(
      final Throwable throwable, final HttpStatus status, final @Nullable String defaultDetail) {
    final String errorKey = ProblemUtils.errorKey(throwable.getClass());
    return toProblemDetails(errorKey, status, defaultDetail);
  }

  /**
   * Builds problem details for an error key, logging the key before resolution.
   *
   * @param errorKey the message-source key used to resolve code, title, and detail
   * @param status the HTTP status of the problem
   * @param defaultDetail the detail used when no message is configured for {@code errorKey}
   * @return problem details for the error key
   */
  default ProblemDetails toProblemDetails(
      final String errorKey, final HttpStatus status, final @Nullable String defaultDetail) {
    return toProblemDetails(errorKey, status, defaultDetail, (Object[]) null);
  }

  /**
   * Builds problem details for an error key, logging the key before resolution.
   *
   * <p>{@code detailArgs} fill the placeholders of the configured detail message, or of {@code
   * defaultDetail} when none is configured.
   *
   * @param errorKey the message-source key used to resolve code, title, and detail
   * @param status the HTTP status of the problem
   * @param defaultDetail the detail used when no message is configured for {@code errorKey}
   * @param detailArgs arguments substituted into the detail message, or {@code null} when the
   *     detail has none
   * @return problem details for the error key
   */
  default ProblemDetails toProblemDetails(
      final String errorKey,
      final HttpStatus status,
      final @Nullable String defaultDetail,
      final Object @Nullable ... detailArgs) {
    logErrorKey(errorKey);
    return ProblemDetails.of(errorKey, defaultDetail, detailArgs, status);
  }

  /**
   * Completes problem details for the current request and logs the throwable.
   *
   * <p>Sets the request URI as the problem instance and the HTTP method on the details. When {@link
   * #typeUrl()} is set, also sets the problem type to that URL with the problem code as fragment,
   * such as {@code https://example.com/problems#order-not-found}.
   *
   * @param problemDetails the problem details to complete
   * @param request the current web request
   * @param throwable the throwable being handled
   * @return the completed problem details
   */
  default ProblemDetails toResponse(
      final ProblemDetails problemDetails,
      final NativeWebRequest request,
      final Throwable throwable) {
    problemDetails.setType(typeUri(problemDetails));
    problemDetails.setInstance(requestUri(request));
    problemDetails.setMethod(requestMethod(request));
    log(throwable, problemDetails);
    return problemDetails;
  }

  /**
   * Sets the given headers on the servlet response, replacing any existing values.
   *
   * @param request the current web request
   * @param headers the headers to set
   */
  default void setResponseHeaders(final NativeWebRequest request, final HttpHeaders headers) {
    final HttpServletResponse response = request.getNativeResponse(HttpServletResponse.class);
    if (response == null) {
      return;
    }
    headers.forEach(
        (name, values) -> {
          response.setHeader(name, null);
          values.forEach(value -> response.addHeader(name, value));
        });
  }

  /**
   * Returns the request URI of the native servlet request.
   *
   * @param request the current web request
   * @return the request URI
   */
  default URI requestUri(final NativeWebRequest request) {
    return URI.create(servletRequest(request).getRequestURI());
  }

  /**
   * Returns the HTTP method of the native servlet request.
   *
   * @param request the current web request
   * @return the HTTP method
   */
  default HttpMethod requestMethod(final NativeWebRequest request) {
    return HttpMethod.valueOf(servletRequest(request).getMethod());
  }

  /**
   * Returns the native servlet request of the current web request.
   *
   * @param request the current web request
   * @return the servlet request
   * @throws NullPointerException if the request is not a servlet request
   */
  private static HttpServletRequest servletRequest(final NativeWebRequest request) {
    return Objects.requireNonNull(
        request.getNativeRequest(HttpServletRequest.class), "Not a servlet request");
  }

  /**
   * Returns the base URL of the page that documents problem codes, from {@code problem.type-url}.
   *
   * <p>Every advice reads the same value, held by {@link ProblemConfigProvider}. Override this
   * method for an advice that should use another URL, or none.
   *
   * @return the base URL, or {@code null} when problem types stay {@code about:blank}
   */
  default @Nullable URI typeUrl() {
    return ProblemConfigProvider.getTypeUrl();
  }

  /**
   * Returns the problem type for the given problem details.
   *
   * <p>With a {@link #typeUrl()}, a problem whose type is still {@code about:blank} gets the URL
   * with its code as fragment. A type set explicitly on the problem is kept.
   *
   * @param problemDetails the problem details
   * @return the problem type, or {@code null} when the problem has none
   */
  default @Nullable URI typeUri(final ProblemDetails problemDetails) {
    final URI baseUrl = typeUrl();
    final String code = problemDetails.getCode();
    if (baseUrl == null
        || StringUtils.isBlank(code)
        || !DEFAULT_TYPE_URL.equals(problemDetails.getType())) {
      return problemDetails.getType();
    }
    return UriComponentsBuilder.fromUri(baseUrl).fragment(code).encode().build().toUri();
  }

  /**
   * Parses the {@code problem.type-url} property.
   *
   * @param value the property value, may be {@code null} or blank
   * @return the URL, or {@code null} when the value is blank
   * @throws IllegalArgumentException if the value is not an absolute URI
   */
  static @Nullable URI parseTypeUrl(@Nullable final String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }
    final URI url;
    try {
      url = new URI(value.trim());
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException(
          "'problem.type-url' must be an absolute URI, but was: " + value, e);
    }
    if (!url.isAbsolute()) {
      throw new IllegalArgumentException(
          "'problem.type-url' must be an absolute URI, but was: " + value);
    }
    return url;
  }

  /**
   * Logs the throwable for the problem it was turned into.
   *
   * <p>A server error is logged at error level with its stack trace. Any other status, typically a
   * client error, is logged as a single warn line; its stack trace is logged at debug level.
   *
   * @param throwable the throwable
   * @param problemDetails the problem details sent to the client, with method and instance set
   */
  default void log(final Throwable throwable, final ProblemDetails problemDetails) {
    final int status = problemDetails.getStatus();
    final String exceptionName = ProblemUtils.errorKey(throwable.getClass());
    if (HttpStatusCode.valueOf(status).is5xxServerError()) {
      ExceptionalLog.LOGGER.error(
          ExceptionalLog.PROBLEM_LOG_FORMAT,
          problemDetails.getMethod(),
          problemDetails.getInstance(),
          status,
          problemDetails.getCode(),
          exceptionName,
          throwable.getMessage(),
          throwable);
    } else {
      ExceptionalLog.LOGGER.warn(
          ExceptionalLog.PROBLEM_LOG_FORMAT,
          problemDetails.getMethod(),
          problemDetails.getInstance(),
          status,
          problemDetails.getCode(),
          exceptionName,
          throwable.getMessage());
      ExceptionalLog.LOGGER.debug(
          "Stack trace for {} {}",
          problemDetails.getMethod(),
          problemDetails.getInstance(),
          throwable);
    }
  }

  /**
   * Logs an exception key at trace level.
   *
   * @param exceptionKey the exception key to log
   */
  default void logExceptionKey(final String exceptionKey) {
    ExceptionalLog.LOGGER.trace(
        ExceptionalLog.KEYS_LOG_FORMAT,
        System.lineSeparator(),
        ExceptionalLog.EXCEPTION_KEY_LOG_START,
        System.lineSeparator(),
        exceptionKey,
        System.lineSeparator(),
        ExceptionalLog.KEYS_LOG_END);
  }

  /**
   * Logs a single error key at trace level.
   *
   * @param errorKey the error key to log
   */
  default void logErrorKey(final String errorKey) {
    ExceptionalLog.LOGGER.trace(
        ExceptionalLog.KEYS_LOG_FORMAT,
        System.lineSeparator(),
        ExceptionalLog.ERROR_KEYS_LOG_START,
        System.lineSeparator(),
        errorKey,
        System.lineSeparator(),
        ExceptionalLog.KEYS_LOG_END);
  }

  /**
   * Logs several error keys at trace level.
   *
   * @param errorKeys the error keys to log
   */
  default void logErrorKeys(final String[] errorKeys) {
    if (ExceptionalLog.LOGGER.isTraceEnabled()) {
      ExceptionalLog.LOGGER.trace(
          ExceptionalLog.KEYS_LOG_FORMAT,
          System.lineSeparator(),
          ExceptionalLog.ERROR_KEYS_LOG_START,
          System.lineSeparator(),
          String.join(System.lineSeparator(), errorKeys),
          System.lineSeparator(),
          ExceptionalLog.KEYS_LOG_END);
    }
  }
}
