package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.autoconfigure.DatabaseConstraintResolverConfiguration;
import com.neolama.common.exception.autoconfigure.ProblemConfiguration;
import com.neolama.common.exception.autoconfigure.WebDaoExceptionHandler;
import com.neolama.common.exception.autoconfigure.WebExceptionHandler;
import com.neolama.common.exception.autoconfigure.WebSecurityExceptionHandler;
import com.neolama.common.exception.support.AdviceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.servlet.HandlerExceptionResolver;

/** With {@code problem.type-url} set, the problem type is that URL with the code as fragment. */
@AdviceTest
@TestPropertySource(properties = "problem.type-url=" + TypeUrlTest.BASE)
class TypeUrlTest {

  static final String BASE = "https://example.com/problems";

  @Autowired private MockMvcTester mvc;

  private void assertType(final String uri, final String expectedType) {
    assertThat(this.mvc.get().uri(uri)).bodyJson().extractingPath("$.type").isEqualTo(expectedType);
  }

  @Test
  void generalHandlerSetsType() {
    assertType("/does-not-exist", BASE + "#no-resource-found-exception");
  }

  @Test
  void daoHandlerSetsType() {
    assertType("/dao/duplicate-postgres-unconfigured", BASE + "#conflict");
  }

  @Test
  void securityHandlerSetsType() {
    assertType("/security/username-not-found", BASE + "#bad-credentials-exception");
  }

  @Test
  void thrownProblemGetsType() {
    assertType("/general/problem-not-found", BASE + "#not-found");
  }

  @Test
  void codeIsEncodedInFragment() {
    assertType("/general/spaced-code", BASE + "#state%20error");
  }

  @Nested
  class InvalidTypeUrl {

    private final WebApplicationContextRunner runner =
        new WebApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    ProblemConfiguration.class,
                    DatabaseConstraintResolverConfiguration.class,
                    WebDaoExceptionHandler.class,
                    WebSecurityExceptionHandler.class,
                    WebExceptionHandler.class))
            .withBean(
                "handlerExceptionResolver",
                HandlerExceptionResolver.class,
                () -> (request, response, handler, exception) -> null);

    @Test
    void relativeUrlFailsStartup() {
      this.runner
          .withPropertyValues("problem.type-url=problems")
          .run(
              context ->
                  assertThat(context)
                      .getFailure()
                      .hasStackTraceContaining("'problem.type-url' must be an absolute URI"));
    }

    @Test
    void malformedUrlFailsStartup() {
      this.runner
          .withPropertyValues("problem.type-url=http://exa mple.com")
          .run(
              context ->
                  assertThat(context)
                      .getFailure()
                      .hasStackTraceContaining("'problem.type-url' must be an absolute URI"));
    }
  }
}
