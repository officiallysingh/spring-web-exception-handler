package com.neolama.common.exception.jackson;

import org.springframework.http.HttpStatusCode;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for {@link HttpStatusCode}, read from its number.
 *
 * <p>Known codes become the matching {@link org.springframework.http.HttpStatus}; any other code
 * from 100 to 999, such as {@code 499}, becomes a plain {@link HttpStatusCode}. Any other value is
 * reported as a {@link tools.jackson.databind.exc.MismatchedInputException}. A JSON {@code null} is
 * handled by Jackson and reads as {@code null}.
 */
final class HttpStatusDeserializer extends StdDeserializer<HttpStatusCode> {

  /** Lowest valid status code. */
  private static final int MIN_STATUS = 100;

  /** Highest valid status code. */
  private static final int MAX_STATUS = 999;

  /** Constructs a new {@link HttpStatusCode} deserializer. */
  HttpStatusDeserializer() {
    super(HttpStatusCode.class);
  }

  /** {@inheritDoc} */
  @Override
  public HttpStatusCode deserialize(final JsonParser json, final DeserializationContext context)
      throws JacksonException {
    if (json.isExpectedNumberIntToken()) {
      final int status = json.getIntValue();
      if (status >= MIN_STATUS && status <= MAX_STATUS) {
        return HttpStatusCode.valueOf(status);
      }
    }
    return context.reportInputMismatch(this, "HTTP status must be an integer from 100 to 999");
  }
}
