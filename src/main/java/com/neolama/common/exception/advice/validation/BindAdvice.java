package com.neolama.common.exception.advice.validation;

import com.neolama.common.exception.core.Problem;
import com.neolama.common.exception.core.ProblemDetails;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface BindAdvice extends BaseBindingResultHandlingAdvice {

  /**
   * Handles {@link BindException} and converts it into a response.
   *
   * @param exception the bind exception
   * @param request the request
   * @return the error response
   */
  @ExceptionHandler
  default ProblemDetails handleBindException(
      final BindException exception, final NativeWebRequest request) {
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(exception, HttpStatus.BAD_REQUEST);
    final List<Problem> violations =
        handleBindingResult(exception.getBindingResult(), exception, status);
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, exception.getMessage());
    problemDetails.setViolations(violations);
    return toResponse(problemDetails, request, exception);
  }
}
