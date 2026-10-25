package com.neolama.common.exception.advice.dao;

import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link DataIntegrityViolationException} and returns {@link
 * ProblemDetails}.
 *
 * <p>See {@link BaseDataIntegrityAdvice} for how the error key is built.
 */
public interface DataIntegrityViolationAdvice extends BaseDataIntegrityAdvice {

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
    return handleDataIntegrity(exception, request);
  }
}
