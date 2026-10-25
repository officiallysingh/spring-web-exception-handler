package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.core.ProblemConstant.UPLOAD_SIZE_EXCEEDED_DETAIL;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Advice trait that handles {@link MaxUploadSizeExceededException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#BAD_REQUEST}. The maximum size is taken from the
 * exception, or else from the last number in the most specific cause's message, which is the limit
 * in the messages of Tomcat and Jetty. It is passed to the detail as a readable size such as {@code
 * 10 MB}.
 */
public interface MaxUploadSizeExceededExceptionAdvice extends Exceptional {

  /**
   * Handles an upload that exceeded the configured maximum size.
   *
   * @param exception the max-upload-size exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleMaxUploadSizeExceededException(
      final MaxUploadSizeExceededException exception, final NativeWebRequest request) {
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final long bytes =
        exception.getMaxUploadSize() != -1
            ? exception.getMaxUploadSize()
            : ProblemUtils.lastNumber(exception.getMostSpecificCause().getMessage(), -1);
    final String maxFileSizeAllowed = bytes != -1 ? ProblemUtils.formatSize(bytes) : "UNKNOWN";

    final HttpStatus status = resolveStatus(errorKey, HttpStatus.BAD_REQUEST);
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, UPLOAD_SIZE_EXCEEDED_DETAIL, maxFileSizeAllowed);
    return toResponse(problemDetails, request, exception);
  }
}
