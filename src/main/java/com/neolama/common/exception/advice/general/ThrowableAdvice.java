package com.neolama.common.exception.advice.general;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface ThrowableAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleThrowable(
      final Throwable throwable, final NativeWebRequest request) {
    final ProblemDetails problemDetails = toProblemDetails(throwable);
    return toResponse(problemDetails, request, throwable);
  }
}
