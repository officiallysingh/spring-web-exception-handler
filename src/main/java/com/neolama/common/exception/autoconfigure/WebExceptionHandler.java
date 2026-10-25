package com.neolama.common.exception.autoconfigure;

import com.neolama.common.exception.advice.Exceptional;
import com.neolama.common.exception.advice.general.GeneralAdvice;
import com.neolama.common.exception.advice.http.HttpAdvice;
import com.neolama.common.exception.advice.io.IOAdvice;
import com.neolama.common.exception.advice.routing.RoutingAdvice;
import com.neolama.common.exception.advice.validation.ValidationAdvice;
import java.net.URI;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice that turns general, HTTP, I/O, routing, and validation failures into problem
 * details.
 *
 * <p>Enabled in servlet web applications.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RestControllerAdvice
public class WebExceptionHandler
    implements GeneralAdvice, HttpAdvice, IOAdvice, RoutingAdvice, ValidationAdvice {

  /** Base URL for problem types, from {@code problem.type-url}, or {@code null} when unset. */
  private final @Nullable URI typeUrl;

  /**
   * Creates the handler.
   *
   * @param typeUrl the {@code problem.type-url} property, may be blank
   */
  WebExceptionHandler(@Value("${problem.type-url:}") final String typeUrl) {
    this.typeUrl = Exceptional.parseTypeUrl(typeUrl);
  }

  /** {@inheritDoc} */
  @Override
  public @Nullable URI typeUrl() {
    return this.typeUrl;
  }
}
