package com.neolama.oxneer.common.exception.advice.validation;

import static com.neolama.oxneer.common.exception.core.ProblemConstant.DOT;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface TypeMismatchAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleTypeMismatch(
      final TypeMismatchException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    final String propertyName = exception.getPropertyName();
    final String errorKey = exceptionKey + DOT + exception.getErrorCode() + DOT + propertyName;
    final ProblemDetails problemDetails =
        ProblemDetails.of(errorKey, "Invalid value for property " + propertyName, status);
    return toResponse(problemDetails, request, exception);
  }
}
