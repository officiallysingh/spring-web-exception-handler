package com.neolama.common.exception.advice.dao;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface DataIntegrityViolationAdvice extends BaseDataIntegrityAdvice {

  @ExceptionHandler
  default ProblemDetails handleDataIntegrityViolationException(
      final DataIntegrityViolationException exception, final NativeWebRequest request) {
    final String exceptionMessage = exception.getMostSpecificCause().getMessage().trim();
    final String constraintName = resolveConstraintName(exceptionMessage);

    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    final String errorKey = exceptionKey + DOT + constraintName;
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.INTERNAL_SERVER_ERROR);

    final ProblemDetails problemDetails = toProblemDetails(errorKey, status, exceptionMessage);
    return toResponse(problemDetails, request, exception);
  }
}
