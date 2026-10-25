package com.neolama.common.exception.advice.security;

import static com.neolama.common.exception.core.ProblemConstant.BAD_CREDENTIALS_DETAIL;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link UsernameNotFoundException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#UNAUTHORIZED}.
 */
public interface UsernameNotFoundAdvice extends Exceptional {

  /**
   * Handles a lookup for a username that does not exist.
   *
   * @param exception the username-not-found exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleUsernameNotFoundException(
      final UsernameNotFoundException exception, final NativeWebRequest request) {
    final HttpStatus status = resolveStatus(exception, HttpStatus.UNAUTHORIZED);
    final ProblemDetails problemDetails =
        toProblemDetails(exception, status, BAD_CREDENTIALS_DETAIL);
    return toResponse(problemDetails, request, exception);
  }
}
