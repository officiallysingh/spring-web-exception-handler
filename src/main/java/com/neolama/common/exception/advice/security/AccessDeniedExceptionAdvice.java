package com.neolama.common.exception.advice.security;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface AccessDeniedExceptionAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleAccessDeniedException(
      final AccessDeniedException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.FORBIDDEN;
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
