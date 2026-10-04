package com.neolama.common.exception.advice.http;

import static com.neolama.common.exception.core.ProblemConstant.DETAIL_CODE_PREFIX;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.ProblemDetails;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.*;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link HttpMediaTypeNotAcceptableException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#NOT_ACCEPTABLE}.
 */
public interface HttpMediaTypeNotAcceptableAdvice extends Exceptional {

  /**
   * Handles a request whose {@code Accept} header matches no producible media type.
   *
   * @param exception the not-acceptable exception
   * @param request the current web request
   * @return problem details listing the supported media types
   */
  @ExceptionHandler
  default ProblemDetails handleMediaTypeNotAcceptable(
      final HttpMediaTypeNotAcceptableException exception, final NativeWebRequest request) {
    final List<MediaType> supportedMediaTypes = exception.getSupportedMediaTypes();
    //    final HttpHeaders headers = new HttpHeaders();
    //    headers.setAccept(supportedMediaTypes);
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.NOT_ACCEPTABLE);
    final String defaultDetail =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey,
            "Media Type Not Acceptable, except: {0}",
            MimeTypeUtils.toString(supportedMediaTypes));
    final ProblemDetails problemDetails = toProblemDetails(errorKey, status, defaultDetail);
    return toResponse(problemDetails, request, exception);
  }
}
