package com.neolama.oxneer.common.exception.advice.general;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
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
