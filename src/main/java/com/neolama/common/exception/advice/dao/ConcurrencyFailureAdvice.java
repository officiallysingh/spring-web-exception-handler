package com.neolama.common.exception.advice.dao;

import static com.neolama.common.exception.core.ProblemConstant.CONCURRENCY_FAILURE_DETAIL;
import static com.neolama.common.exception.core.ProblemConstant.OPTIMISTIC_LOCK_DETAIL;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
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
 *       OptimisticLockingFailureException}, and the detail {@value
 *       com.neolama.common.exception.core.ProblemConstant#OPTIMISTIC_LOCK_DETAIL} by default.
 *   <li>Any other concurrency failure, such as a lock timeout, deadlock, or serialization failure,
 *       is temporary contention in the database. Status {@link HttpStatus#SERVICE_UNAVAILABLE},
 *       error key {@code PessimisticLockingFailureException} or, for a plain one, {@code
 *       ConcurrencyFailureException}, and the detail {@value
 *       com.neolama.common.exception.core.ProblemConstant#CONCURRENCY_FAILURE_DETAIL} by default.
 * </ul>
 *
 * <p>The error key is that Spring base class, not the class thrown: JPA throws subclasses such as
 * {@code ObjectOptimisticLockingFailureException}, so one message bundle fits JPA and MongoDB. The
 * log still names the class thrown.
 */
public interface ConcurrencyFailureAdvice extends Exceptional {

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
    final HttpStatus status =
        resolveStatus(errorKey, optimistic ? HttpStatus.CONFLICT : HttpStatus.SERVICE_UNAVAILABLE);
    final ProblemDetails problemDetails =
        toProblemDetails(
            errorKey, status, optimistic ? OPTIMISTIC_LOCK_DETAIL : CONCURRENCY_FAILURE_DETAIL);
    return toResponse(problemDetails, request, exception);
  }
}
