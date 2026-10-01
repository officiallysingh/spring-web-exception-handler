package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public interface MethodArgumentTypeMismatchAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleMethodArgumentTypeMismatch(
      final MethodArgumentTypeMismatchException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final String parameterName = exception.getParameter().getParameterName();
    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    final String parameterPath =
        exception.getParameter().getContainingClass().getSimpleName()
            + DOT
            + exception.getParameter().getMethod().getName()
            + DOT
            + parameterName;
    final String errorKey = exceptionKey + DOT + parameterPath;
    final ProblemDetails problemDetails =
        ProblemDetails.of(errorKey, "Invalid value for parameter " + parameterName, status);
    return toResponse(problemDetails, request, exception);
  }
}
