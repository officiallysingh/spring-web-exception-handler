package com.neolama.oxneer.common.exception.advice.validation;

import static com.neolama.oxneer.common.exception.core.ProblemConstant.*;

import com.neolama.oxneer.common.exception.advice.Exceptional;
import com.neolama.oxneer.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.oxneer.common.exception.core.Problem;
import com.neolama.oxneer.common.exception.core.ProblemMessageSourceResolver;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public interface BaseBindingResultHandlingAdvice extends Exceptional {

  default List<Problem> handleBindingResult(
      final BindingResult bindingResult, final Throwable exception, final HttpStatus status) {

    final Stream<Problem> fieldErrors =
        bindingResult.getFieldErrors().stream()
            .map(fieldError -> handleFieldError(fieldError, exception, status));

    final Stream<Problem> globalErrors =
        bindingResult.getGlobalErrors().stream()
            .map(objectError -> handleObjectError(objectError, exception, status));

    return Stream.concat(fieldErrors, globalErrors).toList();
  }

  default Problem handleFieldError(
      final FieldError fieldError, final Throwable exception, final HttpStatus status) {
    final String prefix = ClassUtils.getShortClassName(exception.getClass());

    final String[] fieldErrorCodes = fieldError.getCodes();
    final String[] propertyErrorKeys =
        Arrays.stream(fieldErrorCodes).map(code -> prefix + DOT + code).toArray(String[]::new);

    logErrorKeys(propertyErrorKeys);

    final ProblemMessageSourceResolver codeResolver =
        ProblemMessageSourceResolver.of(CODE_CODE_PREFIX + prefix, fieldError, status.value());
    final ProblemMessageSourceResolver messageResolver =
        ProblemMessageSourceResolver.of(MESSAGE_CODE_PREFIX + prefix, fieldError);
    final ProblemMessageSourceResolver detailsResolver =
        ProblemMessageSourceResolver.of(
            DETAIL_CODE_PREFIX + prefix,
            fieldError,
            "Invalid value for property " + fieldError.getField());
    return Problem.of(
        ProblemMessageProvider.getMessage(codeResolver),
        ProblemMessageProvider.getMessage(messageResolver),
        ProblemMessageProvider.getMessage(detailsResolver));
  }

  default Problem handleObjectError(
      final ObjectError objectError, final Throwable exception, final HttpStatus status) {
    final String prefix = ClassUtils.getShortClassName(exception.getClass());

    final String[] objectErrorCodes = objectError.getCodes();
    final String[] propertyErrorKeys =
        Arrays.stream(objectErrorCodes).map(code -> prefix + DOT + code).toArray(String[]::new);

    logErrorKeys(propertyErrorKeys);

    final ProblemMessageSourceResolver codeResolver =
        ProblemMessageSourceResolver.of(CODE_CODE_PREFIX + prefix, objectError, status.value());
    final ProblemMessageSourceResolver messageResolver =
        ProblemMessageSourceResolver.of(MESSAGE_CODE_PREFIX + prefix, objectError);
    final ProblemMessageSourceResolver detailsResolver =
        ProblemMessageSourceResolver.of(
            DETAIL_CODE_PREFIX + prefix,
            objectError,
            "Invalid value for property " + objectError.getObjectName());
    return Problem.of(
        ProblemMessageProvider.getMessage(codeResolver),
        ProblemMessageProvider.getMessage(messageResolver),
        ProblemMessageProvider.getMessage(detailsResolver));
  }
}
