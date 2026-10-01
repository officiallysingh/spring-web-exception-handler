package com.neolama.oxneer.common.exception.advice.general;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import com.neolama.oxneer.common.exception.core.ThrowableProblem;
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
