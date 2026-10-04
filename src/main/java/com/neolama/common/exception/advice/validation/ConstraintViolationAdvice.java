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

/**
 * Advice trait that handles {@link ConstraintViolationException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}. Each constraint violation is returned
 * as a problem.
 */
public interface ConstraintViolationAdvice extends Exceptional {

  /**
   * Handles a constraint-violation exception.
   *
   * @param exception the constraint-violation exception
   * @param request the current web request
   * @return problem details whose violations describe each failed constraint
   */
  @ExceptionHandler
  default ProblemDetails handleConstraintViolationException(
      final ConstraintViolationException exception, final NativeWebRequest request) {
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(exception, HttpStatus.BAD_REQUEST);
    final List<Problem> violations =
        exception.getConstraintViolations().stream()
            .map(violation -> handleConstraintViolation(violation, exception, status))
            .toList();
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, exception.getMessage());
    problemDetails.setViolations(violations);
    return toResponse(problemDetails, request, exception);
  }

  /**
   * Converts one constraint violation into a problem, resolving code, message, and detail from the
   * message source.
   *
   * @param violation the constraint violation
   * @param exception the exception being handled, used as the message-source prefix
   * @param status the HTTP status used when resolving the violation code
   * @return a problem for the constraint violation
   */
  default Problem handleConstraintViolation(
      final ConstraintViolation<?> violation,
      final ConstraintViolationException exception,
      final HttpStatus status) {
    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    final String propertyPath = violation.getPropertyPath().toString();
    logErrorKey(exceptionKey + DOT + propertyPath);

    final ProblemMessageSourceResolver codeResolver =
        ProblemMessageSourceResolver.of(CODE_CODE_PREFIX + exceptionKey, violation, status.value());
    final ProblemMessageSourceResolver messageResolver =
        ProblemMessageSourceResolver.of(MESSAGE_CODE_PREFIX + exceptionKey, violation);
    final ProblemMessageSourceResolver detailsResolver =
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
