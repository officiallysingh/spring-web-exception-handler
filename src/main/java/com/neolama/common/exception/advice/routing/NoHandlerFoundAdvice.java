package com.neolama.common.exception.advice.routing;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Advice trait that handles {@link NoHandlerFoundException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#NOT_FOUND}.
 */
public interface NoHandlerFoundAdvice extends Exceptional {

  /**
   * Handles a request that matched no handler.
   *
   * @param exception the no-handler-found exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleNoHandlerFound(
      final NoHandlerFoundException exception, final NativeWebRequest request) {
    final HttpStatus status = resolveStatus(exception, HttpStatus.NOT_FOUND);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
