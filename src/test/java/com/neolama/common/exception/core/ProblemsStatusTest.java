package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.neolama.common.exception.TestApplication;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

/** Status resolution for {@link Problems}: properties win, then the {@link ErrorType}, then 500. */
class ProblemsStatusTest {

  private static ProblemDetails details(final ThrowableProblem problem) {
    return problem.getProblemDetails();
  }

  @Nested
  @SpringBootTest(
      classes = TestApplication.class,
      properties = "spring.messages.basename=i18n/test-problems")
  class WithProperties {

    @Test
    void errorTypeStatusUsedWhenPropertiesHaveNone() {
      final ErrorType type = DefaultProblem.of("plain.type", "Java detail", HttpStatus.CONFLICT);
      final ProblemDetails details = details(Problems.of(type).throwAble());
      assertThat(details.getStatus()).isEqualTo(409);
      assertThat(details.getDetail()).isEqualTo("Java detail");
    }

    @Test
    void propertiesWinOverErrorType() {
      final ErrorType type = DefaultProblem.of("bundled.type", "Java detail", HttpStatus.CONFLICT);
      final ProblemDetails details = details(Problems.of(type).throwAble());
      assertThat(details.getStatus()).isEqualTo(422);
      assertThat(details.getDetail()).isEqualTo("Detail from properties");
    }

    @Test
    void errorTypeWithoutStatusFallsBackTo500() {
      final ErrorType type = DefaultProblem.of("nostatus.type", "Java detail", null);
      assertThat(details(Problems.of(type).throwAble()).getStatus()).isEqualTo(500);
    }

    @Test
    void errorTypeWithoutStatusUsesProperties() {
      final ErrorType type = DefaultProblem.of("nostatus.bundled", "Java detail", null);
      assertThat(details(Problems.of(type).throwAble()).getStatus()).isEqualTo(404);
    }

    @Test
    void errorKeyUsesPropertiesThenFallsBackTo500() {
      assertThat(details(Problems.of("bundled.key").throwAble()).getStatus()).isEqualTo(410);
      assertThat(details(Problems.of("plain.key").throwAble()).getStatus()).isEqualTo(500);
    }

    @Test
    void explicitStatusWinsOverProperties() {
      assertThat(details(Problems.of("bundled.key").throwAble(HttpStatus.CONFLICT)).getStatus())
          .isEqualTo(409);
    }

    @Test
    void collectionFallsBackTo500() {
      final ProblemDetails details =
          details(
              Problems.of(Problem.of("a", "A", "first"), Problem.of("b", "B", "second"))
                  .throwAble());
      assertThat(details.getStatus()).isEqualTo(500);
      assertThat(details.getErrors()).hasSize(2);
    }

    @Test
    void collectionUsesExplicitStatus() {
      assertThat(
              details(Problems.of(Problem.of("a", "A", "first")).throwAble(HttpStatus.BAD_REQUEST))
                  .getStatus())
          .isEqualTo(400);
    }

    @Test
    void nullExplicitStatusIsRejected() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> Problems.of("plain.key").throwAble(null))
          .withMessage("'status' must not be null");
    }
  }

  @Nested
  @SpringBootTest(
      classes = TestApplication.class,
      properties = {
        "spring.messages.basename=i18n/test-problems",
        "spring.messages.use-code-as-default-message=true"
      })
  class WithCodeAsDefaultMessage {

    @Test
    void missingStatusEntryStillFallsBack() {
      final ErrorType type = DefaultProblem.of("plain.type", "Java detail", HttpStatus.CONFLICT);
      assertThat(details(Problems.of(type).throwAble()).getStatus()).isEqualTo(409);
      assertThat(details(Problems.of("plain.key").throwAble()).getStatus()).isEqualTo(500);
    }
  }
}
