package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.core.ProblemConstant.DATA_BUFFER_LIMIT_DETAIL;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link DataBufferLimitException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#INTERNAL_SERVER_ERROR}. A size parsed from the
 * exception message is passed as a detail argument, as a readable size such as {@code 256 KB}; for
 * example {@code Exceeded limit on max bytes to buffer : 262144} gives {@code 256 KB}.
 */
public interface DataBufferLimitExceptionAdvice extends Exceptional {

  /**
   * Handles response data from a downstream service, such as a {@code WebClient} body, that
   * exceeded the configured data-buffer limit.
   *
   * @param exception the data-buffer limit exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleDataBufferLimitException(
      final DataBufferLimitException exception, final NativeWebRequest request) {
    final long bytes = ProblemUtils.firstNumber(exception.getMessage(), -1);
    final String maxBufferSize = bytes != -1 ? ProblemUtils.formatSize(bytes) : "UNKNOWN";
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.INTERNAL_SERVER_ERROR);
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, DATA_BUFFER_LIMIT_DETAIL, maxBufferSize);
    return toResponse(problemDetails, request, exception);
  }
}
