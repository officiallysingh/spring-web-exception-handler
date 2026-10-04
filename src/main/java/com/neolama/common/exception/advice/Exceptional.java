package com.neolama.common.exception.advice;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.apache.commons.lang3.ClassUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Base interface for all advice traits. Provides methods for resolving HTTP status, converting
 * throwables to problems, and building responses.
 */
public interface Exceptional {

  /** Shared logger for advice traits. */
  Logger logger = LoggerFactory.getLogger(Exceptional.class);

  /** Banner written before a logged exception key. */
  String EXCEPTION_KEY_LOG_START = "---------------------- Exception Key ----------------------";

  /** Banner written before logged error keys. */
  String ERROR_KEYS_LOG_START = "---------------------- Error Keys ----------------------";

  /** Banner written after logged exception or error keys. */
  String KEYS_LOG_END = "--------------------------------------------------------";

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
   * Resolves the HTTP status for the given throwable.
   *
   * <p>Looks up a configured status for the throwable's simple class name, then falls back to a
   * status derived from the throwable and finally {@code defaultStatus}.
   *
   * @param throwable the throwable whose status is resolved
   * @param defaultStatus the status used when no configured status applies
   * @return the resolved HTTP status
   */
  default HttpStatus resolveStatus(final Throwable throwable, final HttpStatus defaultStatus) {
    final HttpStatus defStatus = ProblemUtils.resolveStatus(throwable).orElse(defaultStatus);
    final String errorKey = ClassUtils.getShortClassName(throwable.getClass());
    try {
      final String statusCode =
          ProblemMessageProvider.getMessage(
              STATUS_CODE_PREFIX + errorKey, String.valueOf(defStatus.value()));
      return HttpStatus.valueOf(Integer.parseInt(statusCode));
    } catch (final Exception e) {
      logger.warn("Failed to resolve status code for error key: {}", errorKey, e);
      // Ignore on purpose
      return defStatus;
    }
  }

  /**
   * Resolves the HTTP status configured for the given error key.
   *
   * @param errorKey the message-source key suffix used to look up a status code
   * @param defaultStatus the status used when no status is configured or the lookup fails
   * @return the resolved HTTP status
   */
  default HttpStatus resolveStatus(final String errorKey, final HttpStatus defaultStatus) {
    try {
      final String statusCode =
          ProblemMessageProvider.getMessage(
              STATUS_CODE_PREFIX + errorKey, String.valueOf(defaultStatus.value()));
      return HttpStatus.valueOf(Integer.parseInt(statusCode));
    } catch (final Exception e) {
      logger.warn("Failed to resolve status code for error key: {}", errorKey, e);
      // Ignore on purpose
      return defaultStatus;
    }
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
   * @param throwable the throwable to convert
   * @param status the HTTP status of the problem
   * @return problem details for the throwable
   */
  default ProblemDetails toProblemDetails(final Throwable throwable, final HttpStatus status) {
    return toProblemDetails(throwable, status, throwable.getMessage());
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
      final Throwable throwable, final HttpStatus status, final String defaultDetail) {
    final String errorKey = ClassUtils.getShortClassName(throwable.getClass());
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
      final String errorKey, final HttpStatus status, final String defaultDetail) {
    logErrorKey(errorKey);
    return ProblemDetails.of(errorKey, defaultDetail, status);
  }

  /**
   * Completes problem details for the current request and logs the throwable.
   *
   * <p>Sets the request URI as the problem instance and the HTTP method on the details.
   *
   * @param problemDetails the problem details to complete
   * @param request the current web request
   * @param throwable the throwable being handled
   * @return the completed problem details
   */
  default @NonNull ProblemDetails toResponse(
      final ProblemDetails problemDetails,
      final NativeWebRequest request,
      final Throwable throwable) {
    //    problemDetails.setType(typeUri(request, problemDetails.getCode()));
    problemDetails.setInstance(requestUri(request));
    problemDetails.setMethod(requestMethod(request));
    log(throwable, HttpStatus.valueOf(problemDetails.getStatus()));
    return problemDetails;
  }

  /**
   * Returns the request URI of the native servlet request.
   *
   * @param request the current web request
   * @return the request URI
   */
  default URI requestUri(final NativeWebRequest request) {
    return URI.create(request.getNativeRequest(HttpServletRequest.class).getRequestURI());
  }

  /**
   * Returns the HTTP method of the native servlet request.
   *
   * @param request the current web request
   * @return the HTTP method
   */
  default HttpMethod requestMethod(final NativeWebRequest request) {
    return HttpMethod.valueOf(request.getNativeRequest(HttpServletRequest.class).getMethod());
  }

  //  default URI typeUri(final NativeWebRequest request, final String code) {
  //    HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);
  //    StringBuffer url = httpRequest.getRequestURL();
  //    String path = httpRequest.getRequestURI();
  //    String origin = url.substring(0, url.length() - path.length());
  //    return URI.create(origin + "/problems/help.html#" + code);
  //  }

  /**
   * Logs the throwable and status.
   *
   * @param throwable the throwable
   * @param status the HTTP status
   */
  default void log(final Throwable throwable, final HttpStatus status) {
    logger.error(status.getReasonPhrase(), throwable);
    // throwable.printStackTrace();
  }

  // default void log(final Throwable throwable, final HttpStatus status) {
  // if (status.is4xxClientError()) {
  // logger.warn("{}: {}", status.getReasonPhrase(), throwable.getMessage());
  // } else if (status.is5xxServerError()) {
  // logger.error(status.getReasonPhrase(), throwable);
  // }
  // }

  /**
   * Logs an exception key at trace level.
   *
   * @param exceptionKey the exception key to log
   */
  default void logExceptionKey(final String exceptionKey) {
    logger.trace(
        "{}{}{}",
        EXCEPTION_KEY_LOG_START,
        System.lineSeparator(),
        exceptionKey + System.lineSeparator() + KEYS_LOG_END);
  }

  /**
   * Logs a single error key at trace level.
   *
   * @param errorKey the error key to log
   */
  default void logErrorKey(final String errorKey) {
    logger.trace(
        "{}{}{}",
        ERROR_KEYS_LOG_START,
        System.lineSeparator(),
        errorKey + System.lineSeparator() + KEYS_LOG_END);
  }

  /**
   * Logs several error keys at trace level.
   *
   * @param errorKeys the error keys to log
   */
  default void logErrorKeys(final String[] errorKeys) {
    logger.trace(
        "{}\n{}\n{}",
        ERROR_KEYS_LOG_START,
        String.join(System.lineSeparator(), errorKeys),
        KEYS_LOG_END);
  }
}
