package com.neolama.common.exception.advice.general;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ThrowableProblem;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that returns the {@link com.neolama.common.exception.core.ProblemDetails} already
 * carried by a {@link ThrowableProblem}.
 *
 * <p>The messages of a problem are looked up when it is built, so the advice logs the {@linkplain
 * ThrowableProblem#getErrorKey() error key} it was built from at trace level. A problem built from
 * a list of problems has no key, and nothing is logged.
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
  default ProblemDetails handleThrowableProblem(
      final ThrowableProblem exception, final NativeWebRequest request) {
    final String errorKey = exception.getErrorKey();
    if (errorKey != null) {
      logErrorKey(errorKey);
    }
    final ProblemDetails problemDetails = exception.getProblemDetails();
    return toResponse(problemDetails, request, exception);
  }
}
