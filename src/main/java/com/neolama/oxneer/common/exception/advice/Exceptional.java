package com.neolama.oxneer.common.exception.advice;

import static com.neolama.oxneer.common.exception.core.ProblemConstant.*;

import com.neolama.oxneer.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import com.neolama.oxneer.common.exception.core.ProblemUtils;
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

  Logger logger = LoggerFactory.getLogger(Exceptional.class);

  String ERROR_KEYS_LOG_START = "---------------------- Error Keys ----------------------";
  String ERROR_KEYS_LOG_END = "--------------------------------------------------------";

  default HttpStatus resolveStatus(final Throwable throwable) {
    HttpStatus defaultStatus =
        ProblemUtils.resolveStatus(throwable).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    String errorKey = ClassUtils.getShortClassName(throwable.getClass());
    try {
      String statusCode =
          ProblemMessageProvider.getMessage(
              STATUS_CODE_PREFIX + errorKey, String.valueOf(defaultStatus.value()));
      return HttpStatus.valueOf(statusCode);
    } catch (final Exception e) {
      // Ignore on purpose
      return defaultStatus;
    }
  }

  default ProblemDetails toProblemDetails(final Throwable throwable) {
    HttpStatus status = resolveStatus(throwable);
    return toProblemDetails(throwable, status);
  }

  default ProblemDetails toProblemDetails(final Throwable throwable, final HttpStatus status) {
    return toProblemDetails(throwable, status, throwable.getMessage());
  }

  default ProblemDetails toProblemDetails(
      final Throwable throwable, final HttpStatus status, final String defaultDetail) {
    final String errorKey = ClassUtils.getShortClassName(throwable.getClass());
    return toProblemDetails(errorKey, status, defaultDetail);
  }

  default ProblemDetails toProblemDetails(
      final String errorKey, final HttpStatus status, final String defaultDetail) {
    logErrorKey(errorKey);
    return ProblemDetails.of(errorKey, defaultDetail, status);
  }

  default @NonNull ProblemDetails toResponse(
      final ProblemDetails problemDetails,
      final NativeWebRequest request,
      final Throwable throwable) {
    log(throwable, HttpStatus.valueOf(problemDetails.getStatus()));
    problemDetails.setType(typeUri(request, problemDetails.getCode()));
    problemDetails.setInstance(requestUri(request));
    problemDetails.setMethod(requestMethod(request));
    return problemDetails;
  }

  default URI requestUri(final NativeWebRequest request) {
    return URI.create(request.getNativeRequest(HttpServletRequest.class).getRequestURI());
  }

  default HttpMethod requestMethod(final NativeWebRequest request) {
    return HttpMethod.valueOf(request.getNativeRequest(HttpServletRequest.class).getMethod());
  }

  default URI typeUri(final NativeWebRequest request, final String code) {
    HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);
    StringBuffer url = httpRequest.getRequestURL();
    String path = httpRequest.getRequestURI();
    String origin = url.substring(0, url.length() - path.length());
    return URI.create(origin + "/problems/help.html#" + code);
  }

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

  default void logErrorKey(String errorKey) {
    logger.trace(
        "{}{}{}",
        ERROR_KEYS_LOG_START,
        System.lineSeparator(),
        errorKey + System.lineSeparator() + ERROR_KEYS_LOG_END);
  }

  default void logErrorKeys(String[] errorKeys) {
    logger.trace(
        "{}\n{}\n{}",
        ERROR_KEYS_LOG_START,
        String.join(System.lineSeparator(), errorKeys),
        ERROR_KEYS_LOG_END);
  }
}
