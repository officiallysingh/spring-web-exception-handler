package com.neolama.oxneer.common.exception.advice.http;

import static com.neolama.oxneer.common.exception.core.ProblemConstant.DETAIL_CODE_PREFIX;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.*;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.context.request.NativeWebRequest;

public interface BaseNotAcceptableAdvice extends Exceptional {

  default ProblemDetails processMediaTypeNotSupportedException(
      final List<MediaType> supportedMediaTypes,
      final MediaType causeMediaType,
      final Exception exception,
      final NativeWebRequest request) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(supportedMediaTypes);
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
    final String defaultDetail =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey,
            "Media Type: {0} Not Supported, Supported Media Types are: {1}",
            causeMediaType,
            MimeTypeUtils.toString(supportedMediaTypes));
    final ProblemDetails problemDetails = toProblemDetails(errorKey, status, defaultDetail);
    return toResponse(problemDetails, request, exception);
  }
}
