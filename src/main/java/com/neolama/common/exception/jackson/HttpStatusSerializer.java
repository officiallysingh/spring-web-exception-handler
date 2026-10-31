package com.neolama.common.exception.jackson;

import org.springframework.http.HttpStatusCode;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson serializer for {@link HttpStatusCode}, written as its number.
 *
 * <p>Jackson writes a {@code null} value itself and never passes one to this serializer.
 */
final class HttpStatusSerializer extends StdSerializer<HttpStatusCode> {

  /** Constructs a new {@link HttpStatusCode} serializer. */
  HttpStatusSerializer() {
    super(HttpStatusCode.class);
  }

  /** {@inheritDoc} */
  @Override
  public void serialize(HttpStatusCode status, JsonGenerator json, SerializationContext provider)
      throws JacksonException {
    json.writeNumber(status.value());
  }
}
