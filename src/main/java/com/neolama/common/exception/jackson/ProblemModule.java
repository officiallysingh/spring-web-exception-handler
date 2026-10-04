package com.neolama.common.exception.jackson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import tools.jackson.core.Version;
import tools.jackson.core.util.VersionUtil;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleSerializers;

/** Jackson module to serialize and deserialize Problem classes. */
public final class ProblemModule extends JacksonModule {

  /** HTTP status codes this module can deserialize, indexed by numeric code. */
  private final Map<Integer, HttpStatusCode> statuses;

  /**
   * Default constructor that uses {@link HttpStatus} as the status type.
   *
   * @see HttpStatus
   */
  public ProblemModule() {
    this(HttpStatus.class);
  }

  /**
   * Constructs a new problem module with the given status types.
   *
   * @param <E> generic enum type
   * @param types status type enums
   * @throws IllegalArgumentException if there are duplicate status codes across all status types
   */
  @SafeVarargs
  public <E extends Enum<?> & HttpStatusCode> ProblemModule(final Class<? extends E>... types)
      throws IllegalArgumentException {

    this(buildIndex(types));
  }

  /**
   * Constructs a module that deserializes the given status codes.
   *
   * @param statuses status codes indexed by numeric value
   */
  private ProblemModule(final Map<Integer, HttpStatusCode> statuses) {
    this.statuses = statuses;
  }

  /**
   * Indexes the constants of the given status enums by numeric code.
   *
   * <p>The deprecated {@code Checkpoint} status is skipped.
   *
   * @param <E> generic enum type that is also an {@link HttpStatusCode}
   * @param types status type enums
   * @return an unmodifiable index of status code to status
   */
  @SafeVarargs
  private static <E extends Enum<?> & HttpStatusCode> Map<Integer, HttpStatusCode> buildIndex(
      final Class<? extends E>... types) {
    final Map<Integer, HttpStatusCode> index = new HashMap<>();

    for (final Class<? extends E> type : types) {
      for (final E status : type.getEnumConstants()) {
        // Skip depricated status "Checkpoint"
        if (((HttpStatus) status).getReasonPhrase().equalsIgnoreCase("Checkpoint")) {
          continue;
        }
        index.put(status.value(), status);
      }
    }

    return Collections.unmodifiableMap(index);
  }

  /** {@inheritDoc} */
  @Override
  public String getModuleName() {
    return ProblemModule.class.getSimpleName();
  }

  /** {@inheritDoc} */
  @Override
  public Version version() {
    return VersionUtil.versionFor(ProblemModule.class);
  }

  /** {@inheritDoc} */
  @Override
  public void setupModule(final SetupContext context) {
    final SimpleSerializers serializers = new SimpleSerializers();
    serializers.addSerializer(HttpStatusCode.class, new HttpStatusSerializer());
    serializers.addSerializer(HttpMethod.class, new HttpMethodSerializer());
    context.addSerializers(serializers);

    final SimpleDeserializers deserializers = new SimpleDeserializers();
    deserializers.addDeserializer(HttpStatusCode.class, new HttpStatusDeserializer(this.statuses));
    deserializers.addDeserializer(HttpMethod.class, new HttpMethodDeserializer());
    context.addDeserializers(deserializers);
  }
}
