package com.neolama.common.exception.advice.validation;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.Problem;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemMessageSourceResolver;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Advice trait that handles {@link HandlerMethodValidationException} and returns {@link
 * ProblemDetails} with one violation per failed parameter constraint.
 *
 * <p>Spring MVC raises this exception when a constraint placed directly on a controller method
 * parameter fails, such as {@code @RequestParam @Min(0) int page}. The status defaults to {@link
 * HttpStatus#BAD_REQUEST}.
 *
 * <p>Each violation's {@code code}, {@code message}, and {@code details} are looked up with the
 * constraint codes Spring provides, such as {@code Min.page}, {@code Min.int}, and {@code Min},
 * prefixed with {@code code.}, {@code message.}, or {@code detail.} and the exception simple name.
 * The message falls back to the constraint message and the details to {@code Invalid value for
 * parameter {name}}.
 *
 * <p>Message placeholders are the same as for a field of a {@code @Valid} body: {@code {0}} the
 * parameter name, {@code {1}} the rejected value, {@code {2}} the parameter name as a translatable
 * value, and {@code {3}} onwards the constraint attributes in alphabetical order, such as max then
 * min for a size constraint.
 */
public interface HandlerMethodValidationAdvice extends Exceptional {

  /**
   * Handles a failed validation of controller method parameters.
   *
   * @param exception the handler method validation exception
   * @param request the current web request
   * @return problem details with the parameter violations
   */
  @ExceptionHandler
  default ProblemDetails handleHandlerMethodValidation(
      final HandlerMethodValidationException exception, final NativeWebRequest request) {
    final String errorKey = ClassUtils.getShortClassName(exception.getClass());
    final HttpStatus status = resolveStatus(errorKey, HttpStatus.BAD_REQUEST);
    final List<Problem> violations =
        exception.getParameterValidationResults().stream()
            .flatMap(
                result ->
                    result.getResolvableErrors().stream()
                        .map(error -> handleParameterError(result, error, errorKey, status)))
            .toList();
    final ProblemDetails problemDetails =
        toProblemDetails(errorKey, status, VALIDATION_FAILED_DETAIL);
    problemDetails.setViolations(violations);
    return toResponse(problemDetails, request, exception);
  }

  /**
   * Builds the violation for one failed constraint on a method parameter.
   *
   * @param result the validation result of the parameter
   * @param error the failed constraint
   * @param prefix the exception simple name used to prefix message codes
   * @param status the HTTP status of the problem
   * @return the violation
   */
  default Problem handleParameterError(
      final ParameterValidationResult result,
      final MessageSourceResolvable error,
      final String prefix,
      final HttpStatus status) {
    final String parameterName =
        Objects.requireNonNullElse(
            result.getMethodParameter().getParameterName(),
            "arg" + result.getMethodParameter().getParameterIndex());
    final String[] errorCodes = Objects.requireNonNullElse(error.getCodes(), new String[0]);
    final Object[] arguments =
        ArrayUtils.addAll(new Object[] {parameterName, result.getArgument()}, error.getArguments());
    logErrorKeys(prefixed(prefix, errorCodes));

    final String code =
        ProblemMessageProvider.getMessage(
            ProblemMessageSourceResolver.of(
                prefixed(CODE_CODE_PREFIX + prefix, errorCodes),
                String.valueOf(status.value()),
                arguments));
    final String message =
        ProblemMessageProvider.getMessage(
            ProblemMessageSourceResolver.of(
                prefixed(MESSAGE_CODE_PREFIX + prefix, errorCodes),
                Objects.requireNonNullElse(error.getDefaultMessage(), ""),
                arguments));
    final String details =
        ProblemMessageProvider.getMessage(
            ProblemMessageSourceResolver.of(
                prefixed(DETAIL_CODE_PREFIX + prefix, errorCodes),
                "Invalid value for parameter " + parameterName,
                arguments));
    return Problem.of(code, message, details);
  }

  /**
   * Prefixes each code with {@code prefix} and a dot.
   *
   * @param prefix the prefix
   * @param codes the codes
   * @return the prefixed codes
   */
  private static String[] prefixed(final String prefix, final String[] codes) {
    return Arrays.stream(codes).map(code -> prefix + DOT + code).toArray(String[]::new);
  }
}
