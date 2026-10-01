package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

public interface UnsupportedMediaTypeStatusAdvice extends BaseNotAcceptableAdvice {

  @ExceptionHandler
  default ProblemDetails handleUnsupportedMediaTypeStatusException(
      final UnsupportedMediaTypeStatusException exception, final NativeWebRequest request) {
    return processMediaTypeNotSupportedException(
        exception.getSupportedMediaTypes(), exception.getContentType(), exception, request);
  }
}
