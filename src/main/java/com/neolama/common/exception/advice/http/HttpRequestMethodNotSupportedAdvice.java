package com.neolama.common.exception.advice.http;

import static com.neolama.common.exception.core.ProblemConstant.DETAIL_CODE_PREFIX;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.ProblemDetails;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Advice trait that handles {@link HttpRequestMethodNotSupportedException} and returns {@link
 * ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#METHOD_NOT_ALLOWED}.
 */
public interface HttpRequestMethodNotSupportedAdvice extends Exceptional {

  /**
   * Handles a request whose HTTP method is not supported by the matched handler.
   *
   * @param exception the method-not-supported exception
   * @param request the current web request
   * @return problem details listing the requested and allowed methods
   */
  @ExceptionHandler
  default ProblemDetails handleRequestMethodNotSupportedException(
      final HttpRequestMethodNotSupportedException exception, final NativeWebRequest request) {
    @Nullable final String[] methods = exception.getSupportedMethods();
    final String requestedMethod = exception.getMethod();
    final String allowedMethods = ArrayUtils.isEmpty(methods) ? "None" : String.join(",", methods);
    //    final HttpHeaders headers = new HttpHeaders();
    //    headers.setAllow(requireNonNull(exception.getSupportedHttpMethods()));
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.METHOD_NOT_ALLOWED);
    final String defaultDetail =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey,
            "Requested Method: {0} not allowed, allowed methods are: {1}",
            requestedMethod,
            allowedMethods);
    final ProblemDetails problemDetails = toProblemDetails(errorKey, status, defaultDetail);
    return toResponse(problemDetails, request, exception);
  }
}
