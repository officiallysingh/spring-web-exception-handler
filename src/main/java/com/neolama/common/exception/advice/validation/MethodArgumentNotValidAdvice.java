package com.neolama.common.exception.advice.validation;

import com.neolama.common.exception.core.Problem;
import com.neolama.common.exception.core.ProblemDetails;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link MethodArgumentNotValidException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}. Binding errors are returned as
 * violations.
 */
public interface MethodArgumentNotValidAdvice extends BaseBindingResultHandlingAdvice {

  /**
   * Handles a {@code @Valid} argument that failed validation.
   *
   * @param exception the method-argument validation exception
   * @param request the current web request
   * @return problem details whose violations describe each binding error
   */
  @ExceptionHandler
  default ProblemDetails handleMethodArgumentNotValid(
      final MethodArgumentNotValidException exception, final NativeWebRequest request) {
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
