package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.core.ProblemConstant.DETAIL_CODE_PREFIX;

import com.google.common.base.CharMatcher;
import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

public interface MaxUploadSizeExceededExceptionAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleMaxUploadSizeExceededException(
      final MaxUploadSizeExceededException exception, final NativeWebRequest request) {
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    String defaultMessage = exception.getMessage();
    long bytes = exception.getMaxUploadSize();
    if (bytes == -1 && exception.getCause() instanceof IllegalStateException e) {
      try {
        defaultMessage = e.getMessage();
        final String byteSizeString =
            defaultMessage.substring(
                defaultMessage.lastIndexOf("(") + 1, defaultMessage.lastIndexOf(")"));
        bytes = StringUtils.isNotBlank(byteSizeString) ? Long.parseLong(byteSizeString) : bytes;
      } catch (final Exception e1) {
        // Ignore on purpose
      }
    }
    if (bytes == -1
        && exception.getMostSpecificCause() instanceof FileSizeLimitExceededException e) {
      try {
        defaultMessage = e.getMessage();
        final String byteSizeString = CharMatcher.inRange('0', '9').retainFrom(defaultMessage);
        bytes = StringUtils.isNotBlank(byteSizeString) ? Long.parseLong(byteSizeString) : bytes;
      } catch (final Exception e1) {
        // Ignore on purpose
      }
    }
    final String maxFileSizeAllowed = bytes != -1 ? DataSize.ofBytes(bytes).toString() : "UNKNOWN";

    final HttpStatus status = resolveStatus(errorKey, HttpStatus.BAD_REQUEST);
    final String defaultDetail =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey, defaultMessage, maxFileSizeAllowed);
    final ProblemDetails problemDetails = toProblemDetails(errorKey, status, defaultDetail);
    return toResponse(problemDetails, request, exception);
  }
}
