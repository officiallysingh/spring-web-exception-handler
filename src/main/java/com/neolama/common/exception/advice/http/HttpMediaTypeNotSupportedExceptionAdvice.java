package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link HttpMediaTypeNotSupportedException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#UNSUPPORTED_MEDIA_TYPE}. The detail lists the
 * rejected media type and the supported ones, which are also sent in the {@code Accept} header.
 */
public interface HttpMediaTypeNotSupportedExceptionAdvice extends Exceptional {

  /**
   * Handles a request whose {@code Content-Type} is not supported.
   *
   * @param exception the unsupported-media-type exception
   * @param request the current web request
   * @return problem details listing the rejected and supported media types
   */
  @ExceptionHandler
  default ProblemDetails handleHttpMediaTypeNotSupportedException(
      final HttpMediaTypeNotSupportedException exception, final NativeWebRequest request) {
    final List<MediaType> supportedMediaTypes = exception.getSupportedMediaTypes();
    if (CollectionUtils.isNotEmpty(supportedMediaTypes)) {
      final HttpHeaders headers = new HttpHeaders();
      headers.setAccept(supportedMediaTypes);
      setResponseHeaders(request, headers);
    }
    final String errorKey = ProblemUtils.errorKey(exception.getClass());
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    final ProblemDetails problemDetails =
        toProblemDetails(
            errorKey,
            status,
            "Media Type: {0} Not Supported, Supported Media Types are: {1}",
            Objects.toString(exception.getContentType()),
            MimeTypeUtils.toString(supportedMediaTypes));
    return toResponse(problemDetails, request, exception);
  }
}
