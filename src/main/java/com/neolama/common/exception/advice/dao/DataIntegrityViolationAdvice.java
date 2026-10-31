package com.neolama.common.exception.advice.dao;

import static com.neolama.common.exception.core.ProblemConstant.DEFAULT_DETAIL;
import static com.neolama.common.exception.core.ProblemConstant.DOT;
import static com.neolama.common.exception.core.ProblemConstant.DUPLICATE_KEY_DETAIL;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link DataIntegrityViolationException} and its subclasses, such as
 * {@link DuplicateKeyException}, and returns {@link ProblemDetails}.
 *
 * <p>A <b>duplicate</b> is a {@link DuplicateKeyException}, or any data-integrity violation whose
 * driver error {@link #isUniqueViolation(Throwable) is a unique violation}. Under JPA, Hibernate
 * reports a unique violation as a plain {@link DataIntegrityViolationException}, so the exception
 * class alone does not tell. A duplicate gets:
 *
 * <ul>
 *   <li>the error key {@code DuplicateKeyException.{constraint}}, such as {@code
 *       DuplicateKeyException.employees_name_key}, whichever exception class Spring threw, so one
 *       message bundle fits JDBC, JPA, and MongoDB;
 *   <li>status {@link HttpStatus#CONFLICT} and the detail {@value
 *       com.neolama.common.exception.core.ProblemConstant#DUPLICATE_KEY_DETAIL} by default.
 * </ul>
 *
 * <p>Any other violation, such as a NOT NULL or foreign-key violation, usually means the
 * application let invalid data through. It gets the error key {@code {Exception}.{constraint}},
 * status {@link HttpStatus#INTERNAL_SERVER_ERROR}, and the generic detail by default.
 *
 * <p>Without a resolvable constraint name the error key is the exception name alone. Configured
 * messages win in every case. The database's own message, which can contain data values, is never
 * sent to clients.
 */
public interface DataIntegrityViolationAdvice extends Exceptional {

  /**
   * Resolves the constraint name from the most specific cause of a data-access exception.
   *
   * @param cause the most specific cause
   * @return the resolved constraint name, or {@code null} when no resolver supports the cause
   */
  @Nullable String resolveConstraintName(final Throwable cause);

  /**
   * Returns whether the most specific cause of a data-access exception is a unique violation.
   *
   * @param cause the most specific cause
   * @return {@code true} when a resolver supports the cause and reports a unique violation
   */
  boolean isUniqueViolation(final Throwable cause);

  /**
   * Handles a data-integrity violation.
   *
   * @param exception the data-integrity violation
   * @param request the current web request
   * @return problem details for the violation
   */
  @ExceptionHandler
  default ProblemDetails handleDataIntegrityViolationException(
      final DataIntegrityViolationException exception, final NativeWebRequest request) {
    final Throwable cause = exception.getMostSpecificCause();
    final boolean duplicate =
        exception instanceof DuplicateKeyException || isUniqueViolation(cause);
    final String constraintName = resolveConstraintName(cause);

    final String exceptionKey =
        ProblemUtils.errorKey(duplicate ? DuplicateKeyException.class : exception.getClass());
    final String errorKey =
        constraintName == null ? exceptionKey : exceptionKey + DOT + constraintName;
    final HttpStatus status =
        resolveStatus(errorKey, duplicate ? HttpStatus.CONFLICT : HttpStatus.INTERNAL_SERVER_ERROR);

    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, duplicate ? DUPLICATE_KEY_DETAIL : DEFAULT_DETAIL);
    return toResponse(problemDetails, request, exception);
  }
}
