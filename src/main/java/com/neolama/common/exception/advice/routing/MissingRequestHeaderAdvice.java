package com.neolama.common.exception.advice.routing;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link MissingRequestHeaderException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}. The error key includes the controller,
 * handler method, and header name.
 */
public interface MissingRequestHeaderAdvice extends Exceptional {

  /**
   * Handles a request that omitted a required header.
   *
   * @param exception the missing-header exception
   * @param request the current web request
   * @return problem details for the missing header
   */
  @ExceptionHandler
  default ProblemDetails handleMissingServletRequestParameter(
      final MissingRequestHeaderException exception, final NativeWebRequest request) {
    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    logExceptionKey(exceptionKey);
    final HttpStatus status = resolveStatus(exceptionKey, HttpStatus.BAD_REQUEST);
    final String parameterKey =
        exception.getParameter().getContainingClass().getSimpleName()
            + DOT
            + exception.getParameter().getMethod().getName()
            + DOT
            + exception.getHeaderName();
    final String errorKey = exceptionKey + DOT + parameterKey;
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, exception.getMessage());
    return toResponse(problemDetails, request, exception);
  }
}
