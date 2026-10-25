package com.neolama.common.exception.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.TestApplication;
import com.neolama.common.exception.autoconfigure.DatabaseConstraintResolverConfiguration;
import com.neolama.common.exception.autoconfigure.ProblemConfiguration;
import com.neolama.common.exception.autoconfigure.WebDaoExceptionHandler;
import com.neolama.common.exception.autoconfigure.WebExceptionHandler;
import com.neolama.common.exception.autoconfigure.WebSecurityExceptionHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.servlet.HandlerExceptionResolver;

/** With {@code problem.type-url} set, the problem type is that URL with the code as fragment. */
class TypeUrlTest {

  private static final String BASE = "https://example.com/problems";

  @Nested
  @SpringBootTest(
      classes = TestApplication.class,
      properties = {"problem.type-url=" + BASE, "spring.messages.basename=i18n/test-type-url"})
  @AutoConfigureMockMvc
  class WithTypeUrl {

    @Autowired private MockMvcTester mvc;

    private void assertType(final String uri, final String expectedType) {
      assertThat(this.mvc.get().uri(uri))
          .bodyJson()
          .extractingPath("$.type")
          .isEqualTo(expectedType);
    }

    @Test
    void generalHandlerSetsType() {
      assertType("/does-not-exist", BASE + "#no-resource-found-exception");
    }

    @Test
    void daoHandlerSetsType() {
      assertType("/duplicate-key-postgres-unconfigured", BASE + "#internal-server-error");
    }

    @Test
    void securityHandlerSetsType() {
      assertType("/username-not-found", BASE + "#username-not-found-exception");
    }

    @Test
    void thrownProblemGetsType() {
      assertType("/problem-not-found", BASE + "#not-found");
    }

    @Test
    void codeIsEncodedInFragment() {
      assertType("/illegal-state", BASE + "#state%20error");
    }
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
                      .rootCause()
                      .hasMessageContaining("'problem.type-url' must be an absolute URI"));
    }
  }
}
