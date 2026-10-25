package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.*;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Shared handling for requests whose media type is not supported.
 *
 * <p>The status defaults to {@link HttpStatus#UNSUPPORTED_MEDIA_TYPE}.
 */
public interface BaseNotAcceptableAdvice extends Exceptional {

  /**
   * Builds problem details listing the media type that was rejected and the types that are
   * supported.
   *
   * @param supportedMediaTypes the media types the endpoint accepts
   * @param causeMediaType the media type that was rejected
   * @param exception the exception being handled
   * @param request the current web request
   * @return problem details for the unsupported media type
   */
  default ProblemDetails processMediaTypeNotSupportedException(
      final List<MediaType> supportedMediaTypes,
      final MediaType causeMediaType,
      final Exception exception,
      final NativeWebRequest request) {
    if (CollectionUtils.isNotEmpty(supportedMediaTypes)) {
      final HttpHeaders headers = new HttpHeaders();
      headers.setAccept(supportedMediaTypes);
      setResponseHeaders(request, headers);
    }
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    final ProblemDetails problemDetails =
        toProblemDetails(
            errorKey,
            status,
            "Media Type: {0} Not Supported, Supported Media Types are: {1}",
            causeMediaType,
            MimeTypeUtils.toString(supportedMediaTypes));
    return toResponse(problemDetails, request, exception);
  }
}
