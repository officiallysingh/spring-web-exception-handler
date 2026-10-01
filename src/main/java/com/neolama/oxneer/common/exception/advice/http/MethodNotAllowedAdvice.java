package com.neolama.oxneer.common.exception.advice.http;

import static com.neolama.oxneer.common.exception.core.ProblemConstant.DETAIL_CODE_PREFIX;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import jakarta.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.MethodNotAllowedException;

public interface MethodNotAllowedAdvice extends Exceptional {

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
    final HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
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
