package com.neolama.common.exception.advice.dao;

import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Groups the data-access advice traits handled by {@link
 * com.neolama.common.exception.autoconfigure.WebDaoExceptionHandler}.
 *
 * <p>Spring Data MongoDB translates a {@code WriteConflict}, the error a transaction gets when a
 * concurrent transaction wrote the same document first, into a {@link
 * DataIntegrityViolationException}. When it {@link #isConcurrencyFailure(Throwable) is a
 * concurrency failure}, it is answered as {@link ConcurrencyFailureAdvice} answers a plain {@code
 * ConcurrencyFailureException}, not as a data-integrity violation.
 */
public interface DaoAdvice extends DataIntegrityViolationAdvice, ConcurrencyFailureAdvice {

  /** {@inheritDoc} */
  @Override
  @ExceptionHandler
  default ProblemDetails handleDataIntegrityViolationException(
      final DataIntegrityViolationException exception, final NativeWebRequest request) {
    if (isConcurrencyFailure(exception)) {
      return toConcurrencyFailureResponse(exception, request);
    }
    return DataIntegrityViolationAdvice.super.handleDataIntegrityViolationException(
        exception, request);
  }
}
