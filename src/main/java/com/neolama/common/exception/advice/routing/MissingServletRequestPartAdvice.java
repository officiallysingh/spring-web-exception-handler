package com.neolama.common.exception.advice.routing;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

public interface MissingServletRequestPartAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleMissingServletRequestPart(
      final MissingServletRequestPartException exception, final NativeWebRequest request) {
    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    logExceptionKey(exceptionKey);
    final HttpStatus status = resolveStatus(exceptionKey, HttpStatus.BAD_REQUEST);
    final String errorKey = exceptionKey + DOT + exception.getRequestPartName();
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, exception.getMessage());
    return toResponse(problemDetails, request, exception);
  }
}
