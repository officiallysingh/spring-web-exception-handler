package com.neolama.common.exception.autoconfigure;

import java.util.Map;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

/**
 * Installs a message source for {@link ProblemMessageProvider} in plain unit tests, without a
 * Spring context.
 */
public final class TestMessageSources {

  private TestMessageSources() {}

  /**
   * Installs a message source with the given messages for the current locale.
   *
   * @param messages message codes and texts
   * @return the installed message source
   */
  public static StaticMessageSource install(final Map<String, String> messages) {
    final StaticMessageSource source = new StaticMessageSource();
    messages.forEach(
        (code, text) -> source.addMessage(code, LocaleContextHolder.getLocale(), text));
    new ProblemMessageProvider(source);
    return source;
  }

  /** Restores an empty message source, as before any Spring context starts. */
  public static void reset() {
    new ProblemMessageProvider(new StaticMessageSource());
  }
}
