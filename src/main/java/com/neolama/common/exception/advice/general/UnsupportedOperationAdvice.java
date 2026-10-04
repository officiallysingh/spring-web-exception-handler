package com.neolama.common.exception.advice.general;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link UnsupportedOperationException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#NOT_IMPLEMENTED}.
 */
public interface UnsupportedOperationAdvice extends Exceptional {

  /**
   * Handles an unsupported operation.
   *
   * @param exception the unsupported-operation exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleUnsupportedOperation(
      final UnsupportedOperationException exception, final NativeWebRequest request) {
    final HttpStatus status = resolveStatus(exception, HttpStatus.NOT_IMPLEMENTED);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
