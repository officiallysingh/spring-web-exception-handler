package com.neolama.common.exception.jackson;

import org.springframework.http.HttpStatusCode;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for {@link HttpStatusCode}.
 *
 * <p>Known codes become the matching {@link org.springframework.http.HttpStatus}; any other code,
 * such as {@code 499}, becomes a plain {@link HttpStatusCode}.
 */
final class HttpStatusDeserializer extends StdDeserializer<HttpStatusCode> {

  /** Constructs a new {@link HttpStatusCode} deserializer. */
  HttpStatusDeserializer() {
    super(HttpStatusCode.class);
  }

  /** {@inheritDoc} */
  @Override
  public HttpStatusCode deserialize(JsonParser json, DeserializationContext ctxt)
      throws JacksonException {
    return HttpStatusCode.valueOf(json.getIntValue());
  }
}
