package com.neolama.common.exception.advice.security;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link BadCredentialsException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#UNAUTHORIZED}.
 */
public interface BadCredentialsExceptionAdvice extends Exceptional {

  /**
   * Handles an authentication attempt that presented invalid credentials.
   *
   * @param exception the bad-credentials exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleBadCredentialsException(
      final BadCredentialsException exception, final NativeWebRequest request) {
    final HttpStatus status = resolveStatus(exception, HttpStatus.UNAUTHORIZED);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
