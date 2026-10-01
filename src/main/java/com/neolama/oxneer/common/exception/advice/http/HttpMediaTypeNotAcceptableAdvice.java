package com.neolama.oxneer.common.exception.advice.http;

import static com.neolama.oxneer.common.exception.core.ProblemConstant.DETAIL_CODE_PREFIX;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.*;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface HttpMediaTypeNotAcceptableAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleMediaTypeNotAcceptable(
      final HttpMediaTypeNotAcceptableException exception, final NativeWebRequest request) {
    final List<MediaType> supportedMediaTypes = exception.getSupportedMediaTypes();
    //    final HttpHeaders headers = new HttpHeaders();
    //    headers.setAccept(supportedMediaTypes);
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = HttpStatus.NOT_ACCEPTABLE;
    final String defaultDetail =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey,
            "Media Type Not Acceptable, except: {0}",
            MimeTypeUtils.toString(supportedMediaTypes));
    final ProblemDetails problemDetails = toProblemDetails(errorKey, status, defaultDetail);
    return toResponse(problemDetails, request, exception);
  }
}
