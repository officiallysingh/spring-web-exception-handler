package com.neolama.common.exception.advice.security;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface UsernameNotFoundAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleUsernameNotFoundException(
      final UsernameNotFoundException exception, final NativeWebRequest request) {
    //    final HttpStatus status = resolveStatus(exception, HttpStatus.NOT_FOUND);
    final HttpStatus status = resolveStatus(exception, HttpStatus.UNAUTHORIZED);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
