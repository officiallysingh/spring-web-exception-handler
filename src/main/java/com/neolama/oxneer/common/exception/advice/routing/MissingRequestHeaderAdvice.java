package com.neolama.oxneer.common.exception.advice.routing;

import static com.neolama.oxneer.common.exception.core.ProblemConstant.*;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.oxneer.common.exception.core.ProblemDetails;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

public interface MissingRequestHeaderAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleMissingServletRequestParameter(
      final MissingRequestHeaderException exception, final NativeWebRequest request) {
    final String exceptionKey = ClassUtils.getShortClassName(exception.getClass());
    final String parameterKey =
        exception.getParameter().getContainingClass().getSimpleName()
            + DOT
            + exception.getParameter().getMethod().getName()
            + DOT
            + exception.getHeaderName();
    final String errorKey = exceptionKey + DOT + parameterKey;
    logErrorKey(errorKey);

    final String codeCode = CODE_CODE_PREFIX + DOT + errorKey;
    final String messageCode = TITLE_CODE_PREFIX + DOT + errorKey;
    final String detailCode = DETAIL_CODE_PREFIX + DOT + errorKey;

    final HttpStatus status = HttpStatus.BAD_REQUEST;

    final String code = ProblemMessageProvider.getMessage(codeCode, String.valueOf(status.value()));
    final String title = ProblemMessageProvider.getMessage(messageCode, status.getReasonPhrase());
    final String detail = ProblemMessageProvider.getMessage(detailCode, exception.getMessage());

    final ProblemDetails problemDetails = ProblemDetails.of(status, code, title, detail);
    return toResponse(problemDetails, request, exception);
  }
}
