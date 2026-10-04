package com.neolama.common.exception.advice.routing;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link ServletRequestBindingException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}.
 */
public interface ServletRequestBindingAdvice extends Exceptional {

  /**
   * Handles a failure while binding the servlet request.
   *
   * @param exception the request-binding exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleServletRequestBinding(
      final ServletRequestBindingException exception, final NativeWebRequest request) {
    // TODO: Can improvise
    final HttpStatus status = resolveStatus(exception, HttpStatus.BAD_REQUEST);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
