package com.neolama.common.exception.autoconfigure;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

/**
 * Resolves problem messages from the application {@link MessageSource} for the current locale.
 *
 * <p>The lookup methods are static so that {@link com.neolama.common.exception.core.Problems} and
 * the advice interfaces can use them without injection. Until the bean of this type is created,
 * lookups use an empty message source and so return their default message, with any arguments
 * filled in. This keeps code that builds problems usable in plain unit tests.
 *
 * <p>One message source is held per JVM: the one of the Spring context that created this bean last.
 * A normal application has a single context, so this does not matter there. A test suite that keeps
 * several cached contexts with different message bundles in one JVM should run each in its own
 * fork, as this project's surefire configuration does.
 */
public class ProblemMessageProvider {

  /** Message source used by the static lookup methods. */
  private static volatile MessageSource messageSource = new StaticMessageSource();

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
    return Objects.requireNonNullElse(
        messageSource.getMessage(
            messageCode, null, defaultMessage, LocaleContextHolder.getLocale()),
        defaultMessage);
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
      final String messageCode, final String defaultMessage, final Object @Nullable ... params) {
    return Objects.requireNonNullElse(
        messageSource.getMessage(
            messageCode, params, defaultMessage, LocaleContextHolder.getLocale()),
        defaultMessage);
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
