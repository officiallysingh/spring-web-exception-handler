package com.neolama.common.exception.advice.general;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/** Advice trait that handles any {@link Throwable} not matched by a more specific handler. */
public interface ThrowableAdvice extends Exceptional {

  /**
   * Handles an otherwise unhandled throwable.
   *
   * @param throwable the throwable
   * @param request the current web request
   * @return problem details for the throwable
   */
  @ExceptionHandler
  default ProblemDetails handleThrowable(
      final Throwable throwable, final NativeWebRequest request) {
    final ProblemDetails problemDetails = toProblemDetails(throwable);
    return toResponse(problemDetails, request, throwable);
  }
}
