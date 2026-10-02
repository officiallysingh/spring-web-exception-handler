package com.neolama.common.exception.advice.general;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface UnsupportedOperationAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleUnsupportedOperation(
      final UnsupportedOperationException exception, final NativeWebRequest request) {
    final HttpStatus status = resolveStatus(exception, HttpStatus.NOT_IMPLEMENTED);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
