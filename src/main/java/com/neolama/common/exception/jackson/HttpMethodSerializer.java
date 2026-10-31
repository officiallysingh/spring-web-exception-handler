package com.neolama.common.exception.jackson;

import org.springframework.http.HttpMethod;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson serializer for {@link HttpMethod}, written as its name.
 *
 * <p>Jackson writes a {@code null} value itself and never passes one to this serializer.
 */
final class HttpMethodSerializer extends StdSerializer<HttpMethod> {

  /** Constructs a new {@link HttpMethod} serializer. */
  HttpMethodSerializer() {
    super(HttpMethod.class);
  }

  /** {@inheritDoc} */
  @Override
  public void serialize(HttpMethod method, JsonGenerator json, SerializationContext provider)
      throws JacksonException {
    json.writeString(method.name());
  }
}
