package com.neolama.common.exception.advice.io;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link DataBufferLimitException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}. A size parsed from the exception
 * message is passed as a detail argument.
 */
public interface DataBufferLimitExceptionAdvice extends Exceptional {

  //  org.springframework.core.io.buffer.DataBufferLimitException: Part exceeded the disk usage
  // limit of 1024 bytes

  /**
   * Handles a request that exceeded the configured data-buffer limit.
   *
   * @param exception the data-buffer limit exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleDataBufferLimitException(
      final DataBufferLimitException exception, final NativeWebRequest request) {
    final String defaultMessage = exception.getMessage();
    final long bytes = ProblemUtils.firstNumber(defaultMessage, -1);
    final String maxFileSizeAllowed = bytes != -1 ? DataSize.ofBytes(bytes).toString() : "UNKNOWN";
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.BAD_REQUEST);
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, defaultMessage, maxFileSizeAllowed);
    return toResponse(problemDetails, request, exception);
  }
}
