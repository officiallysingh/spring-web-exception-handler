package com.neolama.common.exception.autoconfigure;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Resolves problem messages from the application {@link MessageSource} for the current locale.
 *
 * <p>A bean of this type must be created before the static lookup methods are used.
 */
public class ProblemMessageProvider {

  /** Message source used by the static lookup methods. */
  private static MessageSource messageSource;

  /**
   * Constructs a new {@code ProblemMessageProvider} with the given {@link MessageSource}.
   *
   * @param messageSource the {@link MessageSource} to be used for message resolution
   */
  ProblemMessageProvider(final MessageSource messageSource) {
    ProblemMessageProvider.messageSource = messageSource;
  }

  /**
   * Resolves a message for the given code and default message using the current locale.
   *
   * @param messageCode the code to lookup
   * @param defaultMessage the default message to return if the lookup fails
   * @return the resolved message
   */
  public static String getMessage(final String messageCode, final String defaultMessage) {
    return messageSource.getMessage(
        messageCode, null, defaultMessage, LocaleContextHolder.getLocale());
  }

  /**
   * Resolves a message for the given code, default message, and arguments using the current locale.
   *
   * @param messageCode the code to lookup
   * @param defaultMessage the default message to return if the lookup fails
   * @param params arguments for the message
   * @return the resolved message
   */
  public static String getMessage(
      final String messageCode, final String defaultMessage, final Object... params) {
    return messageSource.getMessage(
        messageCode, params, defaultMessage, LocaleContextHolder.getLocale());
  }

  /**
   * Resolves a message for the given code, default message, and arguments using the current locale.
   *
   * @param messageCode the code to lookup
   * @param params arguments for the message
   * @return the resolved message
   */
  public static String getMessage(final String messageCode, final Object... params) {
    return messageSource.getMessage(
        messageCode,
        params,
        "No message found in property files with key: " + messageCode,
        LocaleContextHolder.getLocale());
  }

  /**
   * Resolves a message using a {@link MessageSourceResolvable} and the current locale.
   *
   * @param resolvable the resolvable object
   * @return the resolved message
   */
  public static String getMessage(final MessageSourceResolvable resolvable) {
    return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
  }
}
