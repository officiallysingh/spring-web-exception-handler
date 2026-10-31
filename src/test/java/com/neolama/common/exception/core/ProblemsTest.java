package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.neolama.common.exception.autoconfigure.TestMessageSources;
import com.neolama.common.exception.support.TestErrors;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * {@link Problems} builder. Properties win over Java defaults: the status comes from {@code
 * status.{errorKey}}, then the {@link ErrorType}, then 500.
 */
class ProblemsTest {

  @AfterEach
  void resetMessages() {
    TestMessageSources.reset();
  }

  private static ProblemDetails details(final ThrowableProblem problem) {
    return problem.getProblemDetails();
  }

  @Nested
  class WithoutMessages {

    @Test
    void errorKeyUsesJavaDefaults() {
      final ProblemDetails details = details(Problems.of("order.not.found").throwAble());
      assertThat(details.getStatus()).isEqualTo(500);
      assertThat(details.getCode()).isEqualTo("internal-server-error");
      assertThat(details.getTitle()).isEqualTo("Internal Server Error");
      assertThat(details.getDetail()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void errorTypeUsesItsStatusAndDetail() {
      final ProblemDetails details =
          details(Problems.of(TestErrors.ORDER_NOT_FOUND).detailArgs(42).throwAble());
      assertThat(details.getStatus()).isEqualTo(404);
      assertThat(details.getCode()).isEqualTo("order-not-found");
      assertThat(details.getTitle()).isEqualTo("Not Found");
      assertThat(details.getDetail()).isEqualTo("Order 42 not found");
    }

    @Test
    void errorTypeWithoutStatusFallsBackTo500() {
      assertThat(details(Problems.of(TestErrors.NO_STATUS).throwAble()).getStatus()).isEqualTo(500);
    }

    @Test
    void defaultDetailIsUsed() {
      assertThat(
              details(Problems.of("order.not.found").defaultDetail("Order is gone").throwAble())
                  .getDetail())
          .isEqualTo("Order is gone");
    }

    @Test
    void explicitStatusWins() {
      assertThat(
              details(Problems.of(TestErrors.ORDER_NOT_FOUND).throwAble(HttpStatus.GONE))
                  .getStatus())
          .isEqualTo(410);
    }

    @Test
    void collectionFallsBackTo500WithErrors() {
      final ProblemDetails details =
          details(
              Problems.of(Problem.of("a", "A", "first"), Problem.of("b", "B", "second"))
                  .throwAble());
      assertThat(details.getStatus()).isEqualTo(500);
      assertThat(details.getErrors()).extracting(Problem::getCode).containsExactly("a", "b");
    }

    @Test
    void collectionUsesExplicitStatus() {
      assertThat(
              details(
                      Problems.of(List.of(Problem.of("a", "A", "first")))
                          .throwAble(HttpStatus.BAD_REQUEST))
                  .getStatus())
          .isEqualTo(400);
    }

    @Test
    void causeIsKept() {
      final IllegalStateException cause = new IllegalStateException("boom");
      assertThat(Problems.of("order.not.found").cause(cause).throwAble()).hasCause(cause);
    }

    @Test
    void parametersBecomeProperties() {
      final ProblemDetails details =
          details(
              Problems.of("order.not.found")
                  .parameter("orderId", 42)
                  .parameters(Map.of("region", "eu"))
                  .throwAble());
      assertThat(details.getProperties())
          .containsEntry("orderId", 42)
          .containsEntry("region", "eu");
    }

    @Test
    void nullOrEmptyParametersAreIgnored() {
      assertThat(
              details(Problems.of("order.not.found").parameters(null).throwAble()).getProperties())
          .isNullOrEmpty();
      assertThat(
              details(Problems.of("order.not.found").parameters(Map.of()).throwAble())
                  .getProperties())
          .isNullOrEmpty();
    }
  }

  @Nested
  class WithMessages {

    @Test
    void propertiesWinOverErrorType() {
      TestMessageSources.install(
          Map.of(
              "status.configured.error",
              "422",
              "detail.configured.error",
              "Detail from properties"));
      final ProblemDetails details = details(Problems.of(TestErrors.CONFIGURED).throwAble());
      assertThat(details.getStatus()).isEqualTo(422);
      assertThat(details.getDetail()).isEqualTo("Detail from properties");
    }

    @Test
    void errorTypeWithoutStatusUsesProperties() {
      TestMessageSources.install(Map.of("status.NoStatus", "404"));
      assertThat(details(Problems.of(TestErrors.NO_STATUS).throwAble()).getStatus()).isEqualTo(404);
    }

    @Test
    void errorKeyUsesPropertiesThenFallsBackTo500() {
      TestMessageSources.install(Map.of("status.bundled.key", "410"));
      assertThat(details(Problems.of("bundled.key").throwAble()).getStatus()).isEqualTo(410);
      assertThat(details(Problems.of("plain.key").throwAble()).getStatus()).isEqualTo(500);
    }

    @Test
    void explicitStatusWinsOverProperties() {
      TestMessageSources.install(Map.of("status.bundled.key", "410"));
      assertThat(details(Problems.of("bundled.key").throwAble(HttpStatus.CONFLICT)).getStatus())
          .isEqualTo(409);
    }

    @Test
    void invalidStatusEntryIsIgnored() {
      TestMessageSources.install(Map.of("status.bad.key", "abc", "status.unknown.key", "999"));
      assertThat(details(Problems.of("bad.key").throwAble()).getStatus()).isEqualTo(500);
      assertThat(details(Problems.of("unknown.key").throwAble()).getStatus()).isEqualTo(500);
    }

    @Test
    void codeAsDefaultMessageStillFallsBack() {
      TestMessageSources.install(Map.of()).setUseCodeAsDefaultMessage(true);
      assertThat(details(Problems.of(TestErrors.CONFIGURED).throwAble()).getStatus())
          .isEqualTo(409);
      assertThat(details(Problems.of("plain.key").throwAble()).getStatus()).isEqualTo(500);
    }
  }

  @Nested
  class InvalidInput {

    @Test
    void blankErrorKeyIsRejected() {
      assertThatIllegalArgumentException().isThrownBy(() -> Problems.of(" "));
    }

    @Test
    void malformedErrorKeyIsRejected() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> Problems.of("12345"))
          .withMessageContaining("Invalid error key '12345'");
      assertThatIllegalArgumentException()
          .isThrownBy(
              () -> Problems.of(DefaultProblem.of("not found", "detail", HttpStatus.NOT_FOUND)))
          .withMessageContaining("Invalid error key 'not found'");
    }

    @Test
    void errorTypeWithoutKeyIsRejected() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> Problems.of(DefaultProblem.of(" ", "detail", HttpStatus.CONFLICT)));
    }

    @Test
    void emptyOrNullElementCollectionsAreRejected() {
      assertThatIllegalArgumentException().isThrownBy(() -> Problems.of(List.of()));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> Problems.of(Problem.of("a", "A", "first"), (Problem) null));
    }

    @Test
    void reservedOrBlankParameterIsRejected() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> Problems.of("order.not.found").parameter("code", "x"))
          .withMessage("Property code is reserved");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> Problems.of("order.not.found").parameter("", "x"));
    }

    @Test
    void nullExplicitStatusIsRejected() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> Problems.of("plain.key").throwAble(null))
          .withMessage("'status' must not be null");
    }
  }
}
