package com.neolama.common.exception.advice.routing;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Advice trait that handles {@link MissingServletRequestPartException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}. The error key includes the missing part
 * name.
 */
public interface MissingServletRequestPartAdvice extends Exceptional {

  /**
   * Handles a multipart request that omitted a required part.
   *
   * @param exception the missing-part exception
   * @param request the current web request
   * @return problem details for the missing part
   */
  @ExceptionHandler
  default ProblemDetails handleMissingServletRequestPart(
      final MissingServletRequestPartException exception, final NativeWebRequest request) {
    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    logExceptionKey(exceptionKey);
    final HttpStatus status = resolveStatus(exceptionKey, HttpStatus.BAD_REQUEST);
    final String errorKey = exceptionKey + DOT + exception.getRequestPartName();
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, exception.getMessage());
    return toResponse(problemDetails, request, exception);
  }
}
