package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.core.ProblemConstant.DETAIL_CODE_PREFIX;

import com.google.common.base.CharMatcher;
import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface DataBufferLimitExceptionAdvice extends Exceptional {

  //  org.springframework.core.io.buffer.DataBufferLimitException: Part exceeded the disk usage
  // limit of 1024 bytes

  @ExceptionHandler
  default ProblemDetails handleDataBufferLimitException(
      final DataBufferLimitException exception, final NativeWebRequest request) {
    final String defaultMessage = exception.getMessage();
    long bytes = -1;
    try {
      final String byteSizeString = CharMatcher.inRange('0', '9').retainFrom(defaultMessage);
      bytes = StringUtils.isNotBlank(byteSizeString) ? Long.parseLong(byteSizeString) : bytes;
    } catch (final Exception e) {
      // Ignore on purpose
    }

    final String maxFileSizeAllowed = bytes != -1 ? DataSize.ofBytes(bytes).toString() : "UNKNOWN";
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final String defaultDetail =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey, defaultMessage, maxFileSizeAllowed);
    final ProblemDetails problemDetails = toProblemDetails(errorKey, status, defaultDetail);
    return toResponse(problemDetails, request, exception);
  }
}
