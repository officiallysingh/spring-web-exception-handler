package com.neolama.oxneer.common.exception.advice.http;

import com.neolama.oxneer.common.exception.core.ProblemDetails;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface HttpMediaTypeNotSupportedExceptionAdvice extends BaseNotAcceptableAdvice {

  @ExceptionHandler
  default ProblemDetails handleHttpMediaTypeNotSupportedException(
      final HttpMediaTypeNotSupportedException exception, final NativeWebRequest request) {
    return processMediaTypeNotSupportedException(
        exception.getSupportedMediaTypes(), exception.getContentType(), exception, request);
  }
}
