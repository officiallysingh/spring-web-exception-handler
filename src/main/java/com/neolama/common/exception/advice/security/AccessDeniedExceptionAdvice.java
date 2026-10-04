package com.neolama.common.exception.advice.security;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link AccessDeniedException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#FORBIDDEN}.
 */
public interface AccessDeniedExceptionAdvice extends Exceptional {

  /**
   * Handles a request that was authenticated but not authorized.
   *
   * @param exception the access-denied exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleAccessDeniedException(
      final AccessDeniedException exception, final NativeWebRequest request) {
    final HttpStatus status = resolveStatus(exception, HttpStatus.FORBIDDEN);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
