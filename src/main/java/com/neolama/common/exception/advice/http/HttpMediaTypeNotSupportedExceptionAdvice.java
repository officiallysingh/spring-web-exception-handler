package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link HttpMediaTypeNotSupportedException} and returns {@link
 * ProblemDetails}.
 */
public interface HttpMediaTypeNotSupportedExceptionAdvice extends BaseNotAcceptableAdvice {

  /**
   * Handles a request whose {@code Content-Type} is not supported.
   *
   * @param exception the unsupported-media-type exception
   * @param request the current web request
   * @return problem details for the unsupported media type
   */
  @ExceptionHandler
  default ProblemDetails handleHttpMediaTypeNotSupportedException(
      final HttpMediaTypeNotSupportedException exception, final NativeWebRequest request) {
    return processMediaTypeNotSupportedException(
        exception.getSupportedMediaTypes(), exception.getContentType(), exception, request);
  }
}
