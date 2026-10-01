package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.NotAcceptableStatusException;

public interface NotAcceptableStatusAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleMediaTypeNotAcceptable(
      final NotAcceptableStatusException exception, final NativeWebRequest request) {
    // TODO: Can improvise
    final HttpStatus status = HttpStatus.NOT_ACCEPTABLE;
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
