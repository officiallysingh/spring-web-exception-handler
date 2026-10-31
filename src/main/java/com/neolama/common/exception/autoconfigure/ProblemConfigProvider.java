package com.neolama.common.exception.autoconfigure;

import java.net.URI;
import org.jspecify.annotations.Nullable;

/**
 * Holds the library's {@code problem.*} configuration values for every advice, read once at
 * startup.
 *
 * <p>The values are static so that the advice interfaces, including an application's own advices
 * built on {@link com.neolama.common.exception.advice.Exceptional}, can read them without
 * injection. Until the bean of this type is created every value has its default, so code that
 * builds problems stays usable in plain unit tests. As with {@link ProblemMessageProvider}, one set
 * of values is held per JVM: the one of the Spring context that created this bean last.
 */
public class ProblemConfigProvider {

  /** Base URL of the problem types, or {@code null} when {@code problem.type-url} is unset. */
  private static volatile @Nullable URI typeUrl;

  /**
   * Stores the configuration values used by all advices.
   *
   * @param typeUrl the parsed {@code problem.type-url}, or {@code null} when it is unset
   */
  ProblemConfigProvider(final @Nullable URI typeUrl) {
    ProblemConfigProvider.typeUrl = typeUrl;
  }

  /**
   * Returns the base URL of the problem types, from {@code problem.type-url}.
   *
   * @return the base URL, or {@code null} when problem types stay {@code about:blank}
   */
  public static @Nullable URI getTypeUrl() {
    return typeUrl;
  }
}
