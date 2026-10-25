package com.neolama.common.exception.advice.http;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 * Advice trait that handles {@link ResponseStatusException} and returns {@link ProblemDetails}.
 *
 * <p>The status is taken from the exception. A code that is not a known {@link HttpStatus}, such as
 * {@code 499}, becomes {@link HttpStatus#INTERNAL_SERVER_ERROR} for a server error and {@link
 * HttpStatus#BAD_REQUEST} otherwise. The detail defaults to the exception's reason, or to the
 * status reason phrase when there is none.
 */
public interface ResponseStatusAdvice extends Exceptional {

  /**
   * Handles a response-status exception.
   *
   * @param exception the response-status exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleResponseStatusException(
      final ResponseStatusException exception, final NativeWebRequest request) {
    final HttpStatus status = toHttpStatus(exception.getStatusCode());
    final String reason = exception.getReason();
    final String defaultDetail = StringUtils.isNotBlank(reason) ? reason : status.getReasonPhrase();
    final ProblemDetails problemDetails = toProblemDetails(exception, status, defaultDetail);
    return toResponse(problemDetails, request, exception);
  }

  /**
   * Maps a status code to a known {@link HttpStatus}.
   *
   * @param statusCode the status code of the exception
   * @return the matching status, or a generic client or server error status for an unknown code
   */
  private static HttpStatus toHttpStatus(final HttpStatusCode statusCode) {
    final HttpStatus status = HttpStatus.resolve(statusCode.value());
    if (status != null) {
      return status;
    }
    return statusCode.is5xxServerError()
        ? HttpStatus.INTERNAL_SERVER_ERROR
        : HttpStatus.BAD_REQUEST;
  }
}
