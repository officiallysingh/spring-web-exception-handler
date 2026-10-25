package com.neolama.common.exception.core;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import jakarta.validation.ConstraintViolation;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * {@link MessageSourceResolvable} that looks up problem text from one or more message codes.
 *
 * <p>Factory methods accept raw codes, {@link ObjectError}s, {@link FieldError}s, and {@link
 * ConstraintViolation}s, prefixing codes so the text can be externalized.
 *
 * <p>Message placeholders:
 *
 * <ul>
 *   <li>{@link FieldError}: {@code {0}} field, {@code {1}} rejected value, {@code {2}} field name
 *       (translatable), {@code {3}} onwards the constraint attributes in alphabetical order, such
 *       as {@code max} then {@code min} for a size constraint.
 *   <li>{@link ObjectError}: {@code {0}} object name, {@code {1}} object name (translatable),
 *       {@code {2}} onwards the constraint attributes in alphabetical order.
 *   <li>{@link ConstraintViolation}: {@code {0}} property path, {@code {1}} invalid value.
 * </ul>
 */
public class ProblemMessageSourceResolver implements MessageSourceResolvable, Serializable {

  /** Message codes tried in order. */
  private final String[] codes;

  /** Message used when none of the codes is found in the message source. */
  @Nullable private final String defaultMessage;

  /** Arguments substituted into the resolved message. */
  @Nullable private final Object[] arguments;

  /**
   * Creates a resolver for the given codes, default message, and arguments.
   *
   * @param codes the message codes, in lookup order
   * @param defaultMessage the message used when no code is found, or {@code null} when there is
   *     none
   * @param arguments the arguments substituted into the message, or {@code null} when there are
   *     none
   */
  private ProblemMessageSourceResolver(
      final String[] codes, final @Nullable String defaultMessage, Object[] arguments) {
    this.codes = codes;
    this.defaultMessage = defaultMessage;
    this.arguments = arguments;
  }

  /**
   * Creates a resolver for a single error code.
   *
   * @param code the error code
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String code) {
    return new ProblemMessageSourceResolver(new String[] {code}, null, null);
  }

  /**
   * Creates a resolver for multiple error codes.
   *
   * @param codes the error codes
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String[] codes) {
    return new ProblemMessageSourceResolver(codes, null, null);
  }

  /**
   * Creates a resolver for a single error code and arguments.
   *
   * @param code the error code
   * @param arguments the message arguments
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String code, final Object[] arguments) {
    return new ProblemMessageSourceResolver(new String[] {code}, null, arguments);
  }

  /**
   * Creates a resolver for multiple error codes and arguments.
   *
   * @param codes the error codes
   * @param arguments the message arguments
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String[] codes, final Object[] arguments) {
    return new ProblemMessageSourceResolver(codes, null, arguments);
  }

  /**
   * Creates a resolver for multiple error codes and a default message.
   *
   * @param codes the error codes
   * @param defaultMessage the default message
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String[] codes, final String defaultMessage) {
    return new ProblemMessageSourceResolver(codes, defaultMessage, null);
  }

  /**
   * Creates a resolver for multiple error codes and an HTTP status as default message.
   *
   * @param codes the error codes
   * @param status the HTTP status
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String[] codes, final int status) {
    return new ProblemMessageSourceResolver(codes, String.valueOf(status), null);
  }

  /**
   * Creates a resolver for a single error code and a default message.
   *
   * @param code the error code
   * @param defaultMessage the default message
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String code, final String defaultMessage) {
    return new ProblemMessageSourceResolver(new String[] {code}, defaultMessage, null);
  }

  /**
   * Creates a resolver for a single error code and an HTTP status code as default message.
   *
   * @param code the error code
   * @param statusCode the HTTP status code
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String code, final int statusCode) {
    return new ProblemMessageSourceResolver(new String[] {code}, String.valueOf(statusCode), null);
  }

  /**
   * Creates a resolver for multiple error codes, a default message, and arguments.
   *
   * @param codes the error codes
   * @param defaultMessage the default message
   * @param arguments the message arguments
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String[] codes, final String defaultMessage, final Object[] arguments) {
    return new ProblemMessageSourceResolver(codes, defaultMessage, arguments);
  }

  /**
   * Creates a resolver for a single error code, a default message, and arguments.
   *
   * @param code the error code
   * @param defaultMessage the default message
   * @param arguments the message arguments
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String code, final String defaultMessage, final Object[] arguments) {
    return new ProblemMessageSourceResolver(new String[] {code}, defaultMessage, arguments);
  }

  /**
   * Creates a resolver from an {@link ObjectError} with a prefix.
   *
   * @param prefix the prefix for the error codes
   * @param objectError the object error
   * @param defaultMessage the default message
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String prefix, final ObjectError objectError, final String defaultMessage) {
    return of(
        Arrays.stream(objectError.getCodes())
            .map(code -> prefix + DOT + code)
            .toArray(String[]::new),
        defaultMessage,
        objectErrorArguments(objectError));
  }

  /**
   * Creates a resolver from an {@link ObjectError} with a prefix and HTTP status.
   *
   * @param prefix the prefix for the error codes
   * @param objectError the object error
   * @param status the HTTP status
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String prefix, final ObjectError objectError, final int status) {
    return of(
        Arrays.stream(objectError.getCodes())
            .map(code -> prefix + DOT + code)
            .toArray(String[]::new),
        "" + status,
        objectErrorArguments(objectError));
  }

  /**
   * Creates a resolver from an {@link ObjectError} with a prefix. Uses the default message from the
   * {@link ObjectError}.
   *
   * @param prefix the prefix for the error codes
   * @param objectError the object error
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String prefix, final ObjectError objectError) {
    return new ProblemMessageSourceResolver(
        Arrays.stream(objectError.getCodes())
            .map(code -> prefix + DOT + code)
            .toArray(String[]::new),
        objectError.getDefaultMessage(),
        objectErrorArguments(objectError));
  }

  /**
   * Creates a resolver from a {@link FieldError} with a prefix. Uses the default message from the
   * {@link FieldError}.
   *
   * @param prefix the prefix for the error codes
   * @param fieldError the field error
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(final String prefix, final FieldError fieldError) {
    return new ProblemMessageSourceResolver(
        Arrays.stream(fieldError.getCodes())
            .map(code -> prefix + DOT + code)
            .toArray(String[]::new),
        fieldError.getDefaultMessage(),
        fieldErrorArguments(fieldError));
  }

  /**
   * Creates a resolver from a {@link FieldError} with a prefix and default message.
   *
   * @param prefix the prefix for the error codes
   * @param fieldError the field error
   * @param defaultMessage the default message
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String prefix, final FieldError fieldError, final String defaultMessage) {
    return of(
        Arrays.stream(fieldError.getCodes())
            .map(code -> prefix + DOT + code)
            .toArray(String[]::new),
        defaultMessage,
        fieldErrorArguments(fieldError));
  }

  /**
   * Creates a resolver from a {@link FieldError} with a prefix and HTTP status.
   *
   * @param prefix the prefix for the error codes
   * @param fieldError the field error
   * @param status the HTTP status
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String prefix, final FieldError fieldError, final int status) {
    return of(
        Arrays.stream(fieldError.getCodes())
            .map(code -> prefix + DOT + code)
            .toArray(String[]::new),
        "" + status,
        fieldErrorArguments(fieldError));
  }

  /**
   * Creates a resolver from a {@link ConstraintViolation} with a prefix. Uses the message from the
   * {@link ConstraintViolation} as default.
   *
   * @param prefix the prefix for the error code
   * @param violation the constraint violation
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String prefix, final ConstraintViolation<?> violation) {
    return of(
        prefix + DOT + violation.getPropertyPath().toString(),
        violation.getMessage(),
        new Object[] {violation.getPropertyPath().toString(), violation.getInvalidValue()});
  }

  /**
   * Creates a resolver from a {@link ConstraintViolation} with a prefix and default message.
   *
   * @param prefix the prefix for the error code
   * @param violation the constraint violation
   * @param defaultMessage the default message
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String prefix, final ConstraintViolation<?> violation, final String defaultMessage) {
    return of(
        prefix + DOT + violation.getPropertyPath().toString(),
        defaultMessage,
        new Object[] {violation.getPropertyPath().toString(), violation.getInvalidValue()});
  }

  /**
   * Creates a resolver from a {@link ConstraintViolation} with a prefix and HTTP status.
   *
   * @param prefix the prefix for the error code
   * @param violation the constraint violation
   * @param status the HTTP status
   * @return a new {@code ProblemMessageSourceResolver}
   */
  public static ProblemMessageSourceResolver of(
      final String prefix, final ConstraintViolation<?> violation, final int status) {
    return of(
        prefix + DOT + violation.getPropertyPath().toString(),
        "" + status,
        new Object[] {violation.getPropertyPath().toString(), violation.getInvalidValue()});
  }

  /**
   * Builds the message arguments for an {@link ObjectError}.
   *
   * <p>{@code {0}} is the object name; {@code {1}} onwards are the error's own arguments. For a
   * Bean Validation error those are the object name as a resolvable, then the constraint attributes
   * in alphabetical order.
   *
   * @param objectError the object error
   * @return the flattened arguments
   */
  private static Object[] objectErrorArguments(final ObjectError objectError) {
    return concat(new Object[] {objectError.getObjectName()}, objectError.getArguments());
  }

  /**
   * Builds the message arguments for a {@link FieldError}.
   *
   * <p>{@code {0}} is the field, {@code {1}} the rejected value, and {@code {2}} onwards the
   * error's own arguments. For a Bean Validation error those are the field name as a resolvable,
   * which can be translated, then the constraint attributes in alphabetical order. For a size
   * constraint with min 2 and max 5, {@code {3}} is {@code 5} (max) and {@code {4}} is {@code 2}
   * (min).
   *
   * @param fieldError the field error
   * @return the flattened arguments
   */
  private static Object[] fieldErrorArguments(final FieldError fieldError) {
    return concat(
        new Object[] {fieldError.getField(), fieldError.getRejectedValue()},
        fieldError.getArguments());
  }

  /**
   * Appends {@code tail} to {@code head}.
   *
   * @param head the leading arguments
   * @param tail the trailing arguments, may be {@code null}
   * @return a new array with both
   */
  private static Object[] concat(final Object[] head, final Object @Nullable [] tail) {
    return tail == null ? head : ArrayUtils.addAll(head, tail);
  }

  /** {@inheritDoc} */
  @Override
  public String[] getCodes() {
    return this.codes;
  }

  /** {@inheritDoc} */
  @Override
  public @Nullable String getDefaultMessage() {
    return this.defaultMessage;
  }

  /** {@inheritDoc} */
  @Override
  public Object[] getArguments() {
    return this.arguments;
  }
}
