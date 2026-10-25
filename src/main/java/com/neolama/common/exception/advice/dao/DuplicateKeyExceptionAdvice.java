package com.neolama.common.exception.advice.dao;

import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link DuplicateKeyException} and returns {@link ProblemDetails}.
 *
 * <p>See {@link BaseDataIntegrityAdvice} for how the error key is built.
 */
public interface DuplicateKeyExceptionAdvice extends BaseDataIntegrityAdvice {

  /**
   * Handles a duplicate-key violation.
   *
   * @param exception the duplicate-key violation
   * @param request the current web request
   * @return problem details for the violation
   */
  @ExceptionHandler
  default ProblemDetails handleDuplicateKeyException(
      final DuplicateKeyException exception, final NativeWebRequest request) {
    return handleDataIntegrity(exception, request);
  }
}
