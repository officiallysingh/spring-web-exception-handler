package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

public interface ResponseStatusAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleResponseStatusException(
      final ResponseStatusException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
