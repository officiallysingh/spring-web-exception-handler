package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.NotAcceptableStatusException;

/**
 * Advice trait that handles {@link NotAcceptableStatusException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#NOT_ACCEPTABLE}.
 */
public interface NotAcceptableStatusAdvice extends Exceptional {

  /**
   * Handles a reactive request that cannot produce an acceptable response.
   *
   * @param exception the not-acceptable exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleMediaTypeNotAcceptable(
      final NotAcceptableStatusException exception, final NativeWebRequest request) {
    // TODO: Can improvise
    final HttpStatus status = resolveStatus(exception, HttpStatus.NOT_ACCEPTABLE);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
