package com.neolama.common.exception.advice.io;

import static com.neolama.common.exception.core.ProblemConstant.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import com.neolama.common.exception.core.ProblemDetails;
import com.neolama.common.exception.core.ProblemMessageSourceResolver;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

public interface MessageNotReadableAdvice extends Exceptional {

  @ExceptionHandler
  default ProblemDetails handleMessageNotReadableException(
      final HttpMessageNotReadableException exception, final NativeWebRequest request) {
    if (exception.getCause() instanceof InvalidFormatException invalidFormatException) {
      return handleInvalidFormatException(invalidFormatException, request);
    } else {
      final HttpStatus status = resolveStatus(exception, HttpStatus.BAD_REQUEST);
      return toProblemDetails(exception, status);
    }
  }

  default ProblemDetails handleInvalidFormatException(
      final InvalidFormatException invalidFormatException, final NativeWebRequest request) {
    final String exceptionKey = ClassUtils.getShortClassName(invalidFormatException.getClass());
    logExceptionKey(exceptionKey);
    final HttpStatus status = resolveStatus(exceptionKey, HttpStatus.BAD_REQUEST);

    final String[] errorPropertyKeys =
        deriveInvalidFormatExceptionErrorKeys(invalidFormatException);
    logErrorKeys(errorPropertyKeys);

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
            ProblemMessageSourceResolver.of(codeCodes, status.value()));
    final String title =
        ProblemMessageProvider.getMessage(
            ProblemMessageSourceResolver.of(titleCodes, status.getReasonPhrase()));
    final String detail =
        ProblemMessageProvider.getMessage(
            ProblemMessageSourceResolver.of(detailCodes, invalidFormatException.getMessage()));

    final ProblemDetails problemDetails = ProblemDetails.of(status, code, title, detail);
    return toResponse(problemDetails, request, invalidFormatException);
  }

  default String[] deriveInvalidFormatExceptionErrorKeys(
      final InvalidFormatException invalidFormatException) {
    final String errorKey = ClassUtils.getShortClassName(invalidFormatException.getClass());
    final List<JacksonException.Reference> pathRef = invalidFormatException.getPath();
    if (isNotEmpty(pathRef)) {
      String desc = pathRef.get(0).getDescription();
      String packageName = desc.contains("[") ? desc.substring(0, desc.lastIndexOf(".")) : desc;
      List<String> classes =
          pathRef.stream()
              .map(JacksonException.Reference::getDescription)
              .filter(description -> description.contains("[\""))
              .map(
                  description ->
                      description.substring(
                          description.lastIndexOf(".") + 1, description.lastIndexOf("[")))
              .toList();
      String classNames = String.join(".", classes);
      //      Reference ref = pathRef.get(pathRef.size() - 1);
      JacksonException.Reference ref = pathRef.getLast();
      String propertyName = ref.getPropertyName();
      String targetType = ClassUtils.getShortClassName(invalidFormatException.getTargetType());

      return new String[] {
        errorKey + DOT + packageName + DOT + classNames + DOT + propertyName,
        errorKey + DOT + classNames + DOT + propertyName,
        errorKey + DOT + targetType + DOT + propertyName,
        errorKey + DOT + propertyName
      };
    } else {
      return new String[] {errorKey};
    }
  }
}
