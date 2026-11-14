package com.neolama.common.exception.advice.dao;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link ConcurrencyFailureException} and its subclasses, and returns
 * {@link ProblemDetails}.
 *
 * <ul>
 *   <li>An {@link OptimisticLockingFailureException}, such as an {@code @Version} mismatch, means
 *       the record changed since it was read. Status {@link HttpStatus#CONFLICT}, error key {@code
 *       OptimisticLockingFailureException}, and the detail "The resource was changed by another
 *       request; reload it and try again" by default.
 *   <li>Any other concurrency failure, such as a lock timeout, deadlock, or serialization failure,
 *       is temporary contention in the database. Status {@link HttpStatus#SERVICE_UNAVAILABLE},
 *       error key {@code PessimisticLockingFailureException} or, for a plain one, {@code
 *       ConcurrencyFailureException}, and the detail "The request could not be completed because of
 *       concurrent activity; please retry" by default.
 * </ul>
 *
 * <p>The error key is that Spring base class, not the class thrown: JPA throws subclasses such as
 * {@code ObjectOptimisticLockingFailureException}, so one message bundle fits JPA and MongoDB. The
 * log still names the class thrown.
 *
 * <p>Spring Data MongoDB translates an aborted transaction, such as one that lost a write conflict
 * to a concurrent transaction, into a {@link TransientDataAccessException} that is not a {@link
 * ConcurrencyFailureException}. When it {@link #isConcurrencyFailure(Throwable) is a concurrency
 * failure}, it is answered as a plain {@code ConcurrencyFailureException}. Any other transient
 * failure is answered as the {@code Throwable} fallback would answer it.
 */
public interface ConcurrencyFailureAdvice extends Exceptional {

  /**
   * Returns whether a data-access exception is a lost race that a retry can win.
   *
   * @param error the data-access exception
   * @return {@code true} when the exception is a concurrency failure
   * @see com.neolama.common.exception.dao.ConcurrencyFailureDetector
   */
  boolean isConcurrencyFailure(final Throwable error);

  /**
   * Handles a concurrency failure.
   *
   * @param exception the concurrency failure
   * @param request the current web request
   * @return problem details for the failure
   */
  @ExceptionHandler
  default ProblemDetails handleConcurrencyFailure(
      final ConcurrencyFailureException exception, final NativeWebRequest request) {
    return toConcurrencyFailureResponse(exception, request);
  }

  /**
   * Handles a transient data-access failure that is not a {@link ConcurrencyFailureException}.
   *
   * @param exception the transient failure
   * @param request the current web request
   * @return problem details for the failure
   */
  @ExceptionHandler
  default ProblemDetails handleTransientDataAccess(
      final TransientDataAccessException exception, final NativeWebRequest request) {
    if (isConcurrencyFailure(exception)) {
      return toConcurrencyFailureResponse(exception, request);
    }
    final ProblemDetails problemDetails = toProblemDetails(exception);
    return toResponse(problemDetails, request, exception);
  }

  /**
   * Answers a data-access exception as a concurrency failure. An exception that is not a {@link
   * ConcurrencyFailureException}, such as a MongoDB write conflict, gets the error key {@code
   * ConcurrencyFailureException}, and only that key is consulted for its status.
   *
   * @param exception the exception, a concurrency failure or one whose cause is
   * @param request the current web request
   * @return problem details for the failure
   */
  default ProblemDetails toConcurrencyFailureResponse(
      final DataAccessException exception, final NativeWebRequest request) {
    final boolean optimistic = exception instanceof OptimisticLockingFailureException;
    final Class<?> keyClass;
    if (optimistic) {
      keyClass = OptimisticLockingFailureException.class;
    } else if (exception instanceof PessimisticLockingFailureException) {
      keyClass = PessimisticLockingFailureException.class;
    } else {
      keyClass = ConcurrencyFailureException.class;
    }
    final String errorKey = ProblemUtils.errorKey(keyClass);
    final HttpStatus defaultStatus =
        optimistic ? HttpStatus.CONFLICT : HttpStatus.SERVICE_UNAVAILABLE;
    final String thrownKey = ProblemUtils.errorKey(exception.getClass());
    final HttpStatus status =
        thrownKey.equals(errorKey) || !(exception instanceof ConcurrencyFailureException)
            ? resolveStatus(exception, defaultStatus, errorKey)
            : resolveStatus(exception, defaultStatus, thrownKey, errorKey);
    final ProblemDetails problemDetails =
        toProblemDetails(
            errorKey,
            status,
            optimistic
                ? "The resource was changed by another request; reload it and try again"
                : "The request could not be completed because of concurrent activity; please retry");
    return toResponse(problemDetails, request, exception);
  }
}
