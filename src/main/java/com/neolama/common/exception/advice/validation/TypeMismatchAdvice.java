package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link TypeMismatchException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}. The error key includes the error code
 * and property name.
 *
 * <p>A {@link ConversionNotSupportedException} means no converter exists for the target type, a
 * server configuration error rather than a bad value from the client. It is answered like any other
 * unhandled exception: status {@code 500} by default and its simple name as error key.
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
    if (exception instanceof ConversionNotSupportedException) {
      return toResponse(toProblemDetails(exception), request, exception);
    }
    final String exceptionKey = ProblemUtils.errorKey(exception.getClass());
    logExceptionKey(exceptionKey);
    final String propertyName = exception.getPropertyName();
    final String errorKey = exceptionKey + DOT + exception.getErrorCode() + DOT + propertyName;
    final HttpStatus status =
        resolveStatus(new String[] {errorKey, exceptionKey}, HttpStatus.BAD_REQUEST);
    final ProblemDetails problemDetails =
        ProblemDetails.of(errorKey, "Invalid value for property " + propertyName, status);
    return toResponse(problemDetails, request, exception);
  }
}
