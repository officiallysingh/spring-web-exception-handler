package com.neolama.common.exception.advice.general;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ThrowableProblem;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that returns the {@link com.neolama.common.exception.core.ProblemDetails} already
 * carried by a {@link ThrowableProblem}.
 */
public interface ThrowableProblemAdvice extends Exceptional {

  /**
   * Handles an application problem exception.
   *
   * @param exception the problem exception
   * @param request the current web request
   * @return the problem details carried by {@code exception}
   */
  @ExceptionHandler
  default ProblemDetails handleApplicationException(
      final ThrowableProblem exception, final NativeWebRequest request) {
    final ProblemDetails problemDetails = exception.getProblemDetails();
    return toResponse(problemDetails, request, exception);
  }
}
