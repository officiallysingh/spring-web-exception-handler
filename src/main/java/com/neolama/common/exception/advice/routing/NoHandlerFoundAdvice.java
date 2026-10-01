package com.neolama.common.exception.advice.routing;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

public interface NoHandlerFoundAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleNoHandlerFound(
      final NoHandlerFoundException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.NOT_FOUND;
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
