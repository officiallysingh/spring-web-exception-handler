package com.neolama.common.exception.advice.validation;

import com.neolama.common.exception.core.Problem;
import com.neolama.common.exception.core.ProblemDetails;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface MethodArgumentNotValidAdvice extends BaseBindingResultHandlingAdvice {

  @ExceptionHandler
  default ProblemDetails handleMethodArgumentNotValid(
      final MethodArgumentNotValidException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final List<Problem> violations =
        handleBindingResult(exception.getBindingResult(), exception, status);
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, exception.getMessage());
    problemDetails.setViolations(violations);
    return toResponse(problemDetails, request, exception);
  }
}
