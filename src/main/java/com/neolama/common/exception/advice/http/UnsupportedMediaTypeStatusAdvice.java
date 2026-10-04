package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

/**
 * Advice trait that handles {@link UnsupportedMediaTypeStatusException} and returns {@link
 * ProblemDetails}.
 */
public interface UnsupportedMediaTypeStatusAdvice extends BaseNotAcceptableAdvice {

  /**
   * Handles a reactive request whose {@code Content-Type} is not supported.
   *
   * @param exception the unsupported-media-type exception
   * @param request the current web request
   * @return problem details for the unsupported media type
   */
  @ExceptionHandler
  default ProblemDetails handleUnsupportedMediaTypeStatusException(
      final UnsupportedMediaTypeStatusException exception, final NativeWebRequest request) {
    return processMediaTypeNotSupportedException(
        exception.getSupportedMediaTypes(), exception.getContentType(), exception, request);
  }
}
