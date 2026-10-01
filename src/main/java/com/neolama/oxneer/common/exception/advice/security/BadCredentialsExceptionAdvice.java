package com.neolama.oxneer.common.exception.advice.security;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface BadCredentialsExceptionAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetail handleBadCredentialsException(
      final BadCredentialsException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
