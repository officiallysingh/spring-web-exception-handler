package com.neolama.oxneer.common.exception.advice.routing;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface ServletRequestBindingAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleServletRequestBinding(
      final ServletRequestBindingException exception, final NativeWebRequest request) {
    // TODO: Can improvise
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
