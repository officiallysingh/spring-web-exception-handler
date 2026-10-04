package com.neolama.common.exception.advice.io;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartException;

/**
 * Advice trait that handles {@link MultipartException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}.
 */
public interface MultipartAdvice extends Exceptional {

  /**
   * Handles a failure while parsing a multipart request.
   *
   * @param exception the multipart exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleMultipart(
      final MultipartException exception, final NativeWebRequest request) {
    final HttpStatus status = resolveStatus(exception, HttpStatus.BAD_REQUEST);
    final ProblemDetails problemDetails = toProblemDetails(exception, status);
    return toResponse(problemDetails, request, exception);
  }
}
