package com.neolama.common.exception.advice.http;

import static com.neolama.common.exception.core.ProblemConstant.DETAIL_CODE_PREFIX;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.ProblemDetails;
import jakarta.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.MethodNotAllowedException;

/**
 * Advice trait that handles {@link MethodNotAllowedException} and returns {@link ProblemDetails}.
 *
 * <p>The status defaults to {@link HttpStatus#METHOD_NOT_ALLOWED}.
 */
public interface MethodNotAllowedAdvice extends Exceptional {

  /**
   * Handles a reactive request whose HTTP method is not allowed.
   *
   * @param exception the method-not-allowed exception
   * @param request the current web request
   * @return problem details listing the requested and allowed methods
   */
  @ExceptionHandler
  default ProblemDetails handleMethodNotAllowedException(
      final MethodNotAllowedException exception, final NativeWebRequest request) {
    @Nullable final Set<HttpMethod> methods = exception.getSupportedMethods();
    final String requestedMethod = exception.getHttpMethod();
    final String allowedMethods =
        CollectionUtils.isEmpty(methods)
            ? "None"
            : methods.stream().map(HttpMethod::name).collect(Collectors.joining(","));
    //    final HttpHeaders headers = new HttpHeaders();
    //    headers.setAllow(requireNonNull(methods));
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
