package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.Problem;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemMessageSourceResolver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface ConstraintViolationAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleConstraintViolationException(
      final ConstraintViolationException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final List<Problem> violations =
        exception.getConstraintViolations().stream()
            .map(violation -> handleConstraintViolation(violation, exception, status))
            .toList();
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, exception.getMessage());
    problemDetails.setViolations(violations);
    return toResponse(problemDetails, request, exception);
  }

  default Problem handleConstraintViolation(
      final ConstraintViolation<?> violation,
      final ConstraintViolationException exception,
      final HttpStatus status) {
    String exceptionKey = ClassUtils.getShortClassName(exception.getClass());

    String propertyPath = violation.getPropertyPath().toString();

    logErrorKey(exceptionKey + DOT + propertyPath);

    ProblemMessageSourceResolver codeResolver =
        ProblemMessageSourceResolver.of(CODE_CODE_PREFIX + exceptionKey, violation, status.value());
    ProblemMessageSourceResolver messageResolver =
        ProblemMessageSourceResolver.of(MESSAGE_CODE_PREFIX + exceptionKey, violation);
    ProblemMessageSourceResolver detailsResolver =
        ProblemMessageSourceResolver.of(
            DETAIL_CODE_PREFIX + exceptionKey,
            violation,
            "Invalid value for property " + violation.getPropertyPath().toString());
    return Problem.of(
        ProblemMessageProvider.getMessage(codeResolver),
        ProblemMessageProvider.getMessage(messageResolver),
        ProblemMessageProvider.getMessage(detailsResolver));
  }
}
