package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemMessageSourceResolver;
import com.neolama.common.exception.core.ProblemUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Advice trait that handles {@link HttpMessageNotReadableException} and returns {@link
 * ProblemDetails}.
 *
 * <p>Jackson {@link InvalidFormatException} causes are resolved with property-specific error keys.
 * Other causes default to {@link HttpStatus#BAD_REQUEST}.
 */
public interface MessageNotReadableAdvice extends Exceptional {

  /**
   * Handles a request body that could not be read.
   *
   * @param exception the message-not-readable exception
   * @param request the current web request
   * @return problem details for the exception
   */
  @ExceptionHandler
  default ProblemDetails handleMessageNotReadableException(
      final HttpMessageNotReadableException exception, final NativeWebRequest request) {
    if (exception.getCause() instanceof InvalidFormatException invalidFormatException) {
      return handleInvalidFormatException(invalidFormatException, request);
    } else {
      final String exceptionKey = ProblemUtils.errorKey(exception.getClass());
      logExceptionKey(exceptionKey);
      final HttpStatus status = resolveStatus(exceptionKey, HttpStatus.BAD_REQUEST);
      final ProblemDetails problemDetails =
          toProblemDetails(exception, status, MALFORMED_BODY_DETAIL);
      return toResponse(problemDetails, request, exception);
    }
  }

  /**
   * Handles a Jackson value that could not be converted to the target type.
   *
   * @param invalidFormatException the invalid-format exception
   * @param request the current web request
   * @return problem details resolved from the invalid property path
   */
  default ProblemDetails handleInvalidFormatException(
      final InvalidFormatException invalidFormatException, final NativeWebRequest request) {
    final String exceptionKey = ProblemUtils.errorKey(invalidFormatException.getClass());
    logExceptionKey(exceptionKey);
    final String[] errorPropertyKeys =
        deriveInvalidFormatExceptionErrorKeys(invalidFormatException);
    logErrorKeys(errorPropertyKeys);
    // the candidate keys end with the exception name, so its status entry applies to every property
    final HttpStatus status = resolveStatus(errorPropertyKeys, HttpStatus.BAD_REQUEST);

    final String[] codeCodes =
        Arrays.stream(errorPropertyKeys)
            .map(errorKey -> CODE_CODE_PREFIX + errorKey)
            .toArray(String[]::new);
    final String[] titleCodes =
        Arrays.stream(errorPropertyKeys)
            .map(errorKey -> TITLE_CODE_PREFIX + errorKey)
            .toArray(String[]::new);
    final String[] detailCodes =
        Arrays.stream(errorPropertyKeys)
            .map(errorKey -> DETAIL_CODE_PREFIX + errorKey)
            .toArray(String[]::new);
    final String code =
        ProblemMessageProvider.getMessage(
            ProblemMessageSourceResolver.of(codeCodes, ProblemUtils.getProblemCode(status)));
    final String title =
        ProblemMessageProvider.getMessage(
            ProblemMessageSourceResolver.of(titleCodes, status.getReasonPhrase()));
    final String detail =
        ProblemMessageProvider.getMessage(
            ProblemMessageSourceResolver.of(detailCodes, MALFORMED_BODY_DETAIL));

    final ProblemDetails problemDetails = ProblemDetails.of(status, code, title, detail);
    return toResponse(problemDetails, request, invalidFormatException);
  }

  /**
   * Builds message-source keys for an invalid JSON value, from the most specific property path to
   * the exception name alone.
   *
   * <p>For {@code "date"} in {@code com.x.Payload} the keys are {@code
   * InvalidFormatException.com.x.Payload.date}, {@code InvalidFormatException.Payload.date}, {@code
   * InvalidFormatException.LocalDate.date}, {@code InvalidFormatException.date}, and {@code
   * InvalidFormatException}. Nested objects add their class names in path order. A list element
   * reports under the list property.
   *
   * @param invalidFormatException the invalid-format exception
   * @return candidate error keys, most specific first
   */
  default String[] deriveInvalidFormatExceptionErrorKeys(
      final InvalidFormatException invalidFormatException) {
    final String errorKey = ProblemUtils.errorKey(invalidFormatException.getClass());
    final List<JacksonException.Reference> path = invalidFormatException.getPath();
    final List<JacksonException.Reference> properties =
        path.stream().filter(reference -> reference.getPropertyName() != null).toList();
    if (CollectionUtils.isEmpty(properties)) {
      return new String[] {errorKey};
    }
    // An index after the last property, as in "numbers[0]", reports under that property.
    final String propertyName = properties.getLast().getPropertyName();
    final String packageName = packageName(path.getFirst());
    final String classNames =
        properties.stream()
            .map(MessageNotReadableAdvice::simpleClassName)
            .collect(Collectors.joining(DOT));
    final String targetType =
        Objects.nonNull(invalidFormatException.getTargetType())
            ? ClassUtils.getShortClassName(invalidFormatException.getTargetType())
            : "";

    final List<String> keys = new ArrayList<>(5);
    if (StringUtils.isNotEmpty(packageName)) {
      keys.add(errorKey + DOT + packageName + DOT + classNames + DOT + propertyName);
    }
    keys.add(errorKey + DOT + classNames + DOT + propertyName);
    if (StringUtils.isNotEmpty(targetType)) {
      keys.add(errorKey + DOT + targetType + DOT + propertyName);
    }
    keys.add(errorKey + DOT + propertyName);
    keys.add(errorKey);
    return keys.toArray(String[]::new);
  }

  /**
   * Returns the binary name of the class a path element belongs to, such as {@code com.x.Payload}
   * for {@code com.x.Payload["date"]}.
   *
   * <p>Only the text before the first {@code [} is used, so a property name that contains dots
   * cannot change the result.
   *
   * @param reference the path element
   * @return the class name
   */
  private static String className(final JacksonException.Reference reference) {
    final String description = reference.getDescription();
    final int bracket = description.indexOf('[');
    return bracket < 0 ? description : description.substring(0, bracket);
  }

  /**
   * Returns the package of the class a path element belongs to.
   *
   * @param reference the path element
   * @return the package name, or an empty string for a class in the default package
   */
  private static String packageName(final JacksonException.Reference reference) {
    final String className = className(reference);
    final int dot = className.lastIndexOf('.');
    return dot < 0 ? "" : className.substring(0, dot);
  }

  /**
   * Returns the class name without its package, such as {@code Payload} or {@code Outer$Inner}.
   *
   * @param reference the path element
   * @return the simple binary class name
   */
  private static String simpleClassName(final JacksonException.Reference reference) {
    final String className = className(reference);
    return className.substring(className.lastIndexOf('.') + 1);
  }
}
