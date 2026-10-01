package com.neolama.oxneer.common.exception.advice.io;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartException;

public interface MultipartAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleMultipart(
      final MultipartException exception, final NativeWebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
