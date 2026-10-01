package com.neolama.common.exception.advice.general;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ThrowableProblem;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface ThrowableProblemAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleApplicationException(
      final ThrowableProblem exception, final NativeWebRequest request) {
    final ProblemDetails problemDetails = exception.getProblemDetails();
    return toResponse(problemDetails, request, exception);
  }
}
