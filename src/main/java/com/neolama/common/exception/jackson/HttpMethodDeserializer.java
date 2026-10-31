package com.neolama.common.exception.jackson;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for {@link HttpMethod}, read from its name.
 *
 * <p>Only a non-blank JSON string is accepted, such as {@code "GET"}; any other value is reported
 * as a {@link tools.jackson.databind.exc.MismatchedInputException}. A JSON {@code null} is handled
 * by Jackson and reads as {@code null}.
 */
final class HttpMethodDeserializer extends StdDeserializer<HttpMethod> {

  /** Constructs a new {@link HttpMethod} deserializer. */
  HttpMethodDeserializer() {
    super(HttpMethod.class);
  }

  /** {@inheritDoc} */
  @Override
  public HttpMethod deserialize(final JsonParser json, final DeserializationContext context)
      throws JacksonException {
    final String method = json.hasToken(JsonToken.VALUE_STRING) ? json.getString() : null;
    if (StringUtils.isBlank(method)) {
      return context.reportInputMismatch(
          this, "HTTP method must be a non-blank JSON string, such as \"GET\"");
    }
    return HttpMethod.valueOf(method);
  }
}
