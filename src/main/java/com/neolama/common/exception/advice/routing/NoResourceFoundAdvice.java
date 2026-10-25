package com.neolama.common.exception.advice.routing;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.core.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Advice trait that handles {@link NoResourceFoundException} and returns {@link ProblemDetails}.
 *
 * <p>Spring MVC raises this exception when a request path matches no handler and no static
 * resource. The status defaults to {@link HttpStatus#NOT_FOUND}.
 */
public interface NoResourceFoundAdvice extends Exceptional {

  /**
   * Handles a request that matched no handler and no static resource.
   *
   * @param exception the no-resource-found exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleNoResourceFound(
      final NoResourceFoundException exception, final NativeWebRequest request) {
    final HttpStatus status = resolveStatus(exception, HttpStatus.NOT_FOUND);
    final String defaultDetail =
        "No resource found for " + exception.getHttpMethod() + " /" + exception.getResourcePath();
    final ProblemDetails problemDetails = toProblemDetails(exception, status, defaultDetail);
    return toResponse(problemDetails, request, exception);
  }
}
