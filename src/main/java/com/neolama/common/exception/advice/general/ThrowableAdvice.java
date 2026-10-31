package com.neolama.common.exception.advice.general;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles any {@link Throwable} not matched by a more specific handler.
 *
 * <p>The status comes from {@code @ResponseStatus} on the throwable or its cause, then from a
 * Spring {@link org.springframework.web.ErrorResponse}, such as {@code 404} for {@code
 * NoHandlerFoundException} or {@code 400} for a missing cookie, and is otherwise {@code 500}. A
 * {@code status.{SimpleName}} message wins over all of them.
 */
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
