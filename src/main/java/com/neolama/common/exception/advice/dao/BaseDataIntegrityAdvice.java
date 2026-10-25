package com.neolama.common.exception.advice.dao;

import static com.neolama.common.exception.core.ProblemConstant.DATA_CONFLICT_DETAIL;
import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Shared contract for advice traits that turn a database exception into a constraint name.
 *
 * <p>The error key is the exception simple name plus the resolved constraint name, such as {@code
 * DuplicateKeyException.employees_name_key}. When no constraint name can be resolved, the error key
 * is the exception simple name alone.
 */
public interface BaseDataIntegrityAdvice extends Exceptional {

  /**
   * Resolves the constraint name from the most specific cause of a data-access exception.
   *
   * @param cause the most specific cause
   * @return the resolved constraint name, or {@code null} when no resolver supports the cause
   */
  @Nullable String resolveConstraintName(final Throwable cause);

  /**
   * Builds problem details for a data-integrity violation or one of its subclasses.
   *
   * @param exception the data-integrity violation
   * @param request the current web request
   * @return problem details for the violation
   */
  default ProblemDetails handleDataIntegrity(
      final DataIntegrityViolationException exception, final NativeWebRequest request) {
    final Throwable cause = exception.getMostSpecificCause();
    final String constraintName = resolveConstraintName(cause);

    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    final String errorKey =
        constraintName == null ? exceptionKey : exceptionKey + DOT + constraintName;
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.INTERNAL_SERVER_ERROR);

    final ProblemDetails problemDetails = toProblemDetails(errorKey, status, DATA_CONFLICT_DETAIL);
    return toResponse(problemDetails, request, exception);
  }
}
