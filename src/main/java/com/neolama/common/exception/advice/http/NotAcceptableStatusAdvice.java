package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.NotAcceptableStatusException;

/**
 * Advice trait that handles {@link NotAcceptableStatusException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#NOT_ACCEPTABLE}. The detail lists the media types
 * that can be produced, which are also sent in the {@code Accept} header, as for {@link
 * HttpMediaTypeNotAcceptableAdvice}.
 */
public interface NotAcceptableStatusAdvice extends Exceptional {

  /**
   * Handles a reactive request that cannot produce an acceptable response.
   *
   * @param exception the not-acceptable exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleMediaTypeNotAcceptable(
      final NotAcceptableStatusException exception, final NativeWebRequest request) {
    // TODO: Can improvise
    final List<MediaType> supportedMediaTypes = exception.getSupportedMediaTypes();
    if (CollectionUtils.isNotEmpty(supportedMediaTypes)) {
      final HttpHeaders headers = new HttpHeaders();
      headers.setAccept(supportedMediaTypes);
      setResponseHeaders(request, headers);
    }
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(exception, HttpStatus.NOT_ACCEPTABLE);
    final ProblemDetails problemDetails =
        toProblemDetails(
            errorKey,
            status,
            "Media Type Not Acceptable, except: {0}",
            MimeTypeUtils.toString(supportedMediaTypes));
    return toResponse(problemDetails, request, exception);
  }
}
