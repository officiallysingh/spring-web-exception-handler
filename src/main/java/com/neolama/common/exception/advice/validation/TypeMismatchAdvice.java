package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link TypeMismatchException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}. The error key includes the error code
 * and property name.
 */
public interface TypeMismatchAdvice extends Exceptional {

  /**
   * Handles a property value that could not be converted to the required type.
   *
   * @param exception the type-mismatch exception
   * @param request the current web request
   * @return problem details for the invalid property
   */
  @ExceptionHandler
  default ProblemDetails handleTypeMismatch(
      final TypeMismatchException exception, final NativeWebRequest request) {
    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    logExceptionKey(exceptionKey);
    final HttpStatus status = resolveStatus(exceptionKey, HttpStatus.BAD_REQUEST);
    final String propertyName = exception.getPropertyName();
    final String errorKey = exceptionKey + DOT + exception.getErrorCode() + DOT + propertyName;
    final ProblemDetails problemDetails =
        ProblemDetails.of(errorKey, "Invalid value for property " + propertyName, status);
    return toResponse(problemDetails, request, exception);
  }
}
