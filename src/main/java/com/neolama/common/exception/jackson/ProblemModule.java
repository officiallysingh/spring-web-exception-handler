package com.neolama.common.exception.jackson;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import tools.jackson.core.Version;
import tools.jackson.core.util.VersionUtil;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleSerializers;

/**
 * Jackson module that writes {@link HttpStatusCode} as its number and {@link HttpMethod} as its
 * name, and reads them back.
 */
public final class ProblemModule extends JacksonModule {

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
    deserializers.addDeserializer(HttpStatusCode.class, new HttpStatusDeserializer());
    deserializers.addDeserializer(HttpMethod.class, new HttpMethodDeserializer());
    context.addDeserializers(deserializers);
  }
}
