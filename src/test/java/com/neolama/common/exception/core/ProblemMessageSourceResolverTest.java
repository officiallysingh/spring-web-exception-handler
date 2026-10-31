package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.support.ValidatedPayload;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/** {@link ProblemMessageSourceResolver}: codes, default messages, and flat message arguments. */
class ProblemMessageSourceResolverTest {

  private static final String[] CODES = {"Size.payload.nick", "Size"};

  private static final DefaultMessageSourceResolvable FIELD_NAME =
      new DefaultMessageSourceResolvable(new String[] {"payload.nick", "nick"}, "nick");

  private static final Object[] SIZE_ARGUMENTS = {FIELD_NAME, 5, 2};

  @Nested
  class CodeFactories {

    @Test
    void codesAndDefaultMessage() {
      final ProblemMessageSourceResolver resolver = ProblemMessageSourceResolver.of(CODES, "d");
      assertThat(resolver.getCodes()).containsExactly(CODES);
      assertThat(resolver.getDefaultMessage()).isEqualTo("d");
      assertThat(resolver.getArguments()).isNull();
    }

    @Test
    void arguments() {
      final Object[] arguments = {1, 2};
      assertThat(ProblemMessageSourceResolver.of("a", "d", arguments).getCodes())
          .containsExactly("a");
      assertThat(ProblemMessageSourceResolver.of("a", "d", arguments).getArguments())
          .containsExactly(1, 2);
      assertThat(ProblemMessageSourceResolver.of(CODES, "d", arguments).getArguments())
          .containsExactly(1, 2);
    }
  }

  @Nested
  class BindingErrors {

    private final FieldError fieldError =
        new FieldError(
            "payload", "nick", "toolongnick", false, CODES, SIZE_ARGUMENTS, "size must be 2-5");

    private final ObjectError objectError =
        new ObjectError("payload", CODES, SIZE_ARGUMENTS, "invalid payload");

    @Test
    void fieldErrorArgumentsAreFlat() {
      final List<Object> expected = List.of("nick", "toolongnick", FIELD_NAME, 5, 2);
      assertThat(ProblemMessageSourceResolver.of("p", this.fieldError).getArguments())
          .containsExactlyElementsOf(expected);
      assertThat(ProblemMessageSourceResolver.of("p", this.fieldError, "default").getArguments())
          .containsExactlyElementsOf(expected);
      assertThat(ProblemMessageSourceResolver.of("p", this.fieldError, 400).getArguments())
          .containsExactlyElementsOf(expected);
    }

    @Test
    void objectErrorArgumentsAreFlat() {
      final List<Object> expected = List.of("payload", FIELD_NAME, 5, 2);
      assertThat(ProblemMessageSourceResolver.of("p", this.objectError).getArguments())
          .containsExactlyElementsOf(expected);
      assertThat(ProblemMessageSourceResolver.of("p", this.objectError, "default").getArguments())
          .containsExactlyElementsOf(expected);
      assertThat(ProblemMessageSourceResolver.of("p", this.objectError, 400).getArguments())
          .containsExactlyElementsOf(expected);
    }

    @Test
    void errorWithoutArgumentsKeepsOnlyLeadingValues() {
      final FieldError noArguments =
          new FieldError("payload", "nick", "x", false, CODES, null, "bad");
      assertThat(ProblemMessageSourceResolver.of("p", noArguments).getArguments())
          .containsExactly("nick", "x");
    }

    @Test
    void codesArePrefixedAndDefaultsChosen() {
      assertThat(ProblemMessageSourceResolver.of("p", this.fieldError).getCodes())
          .containsExactly("p.Size.payload.nick", "p.Size");
      assertThat(ProblemMessageSourceResolver.of("p", this.fieldError).getDefaultMessage())
          .isEqualTo("size must be 2-5");
      assertThat(ProblemMessageSourceResolver.of("p", this.fieldError, 400).getDefaultMessage())
          .isEqualTo("400");
      assertThat(ProblemMessageSourceResolver.of("p", this.objectError).getDefaultMessage())
          .isEqualTo("invalid payload");
    }
  }

  @Nested
  class ConstraintViolations {

    private final ConstraintViolation<ValidatedPayload> violation;

    ConstraintViolations() {
      try (var factory = Validation.buildDefaultValidatorFactory()) {
        final Validator validator = factory.getValidator();
        this.violation = validator.validate(new ValidatedPayload("", null)).iterator().next();
      }
    }

    @Test
    void codeIsPrefixedPropertyPath() {
      assertThat(ProblemMessageSourceResolver.of("p", this.violation).getCodes())
          .containsExactly("p.name");
    }

    @Test
    void argumentsArePathAndInvalidValue() {
      assertThat(ProblemMessageSourceResolver.of("p", this.violation).getArguments())
          .containsExactly("name", "");
    }

    @Test
    void defaultMessageIsViolationMessageOrGiven() {
      assertThat(ProblemMessageSourceResolver.of("p", this.violation).getDefaultMessage())
          .isEqualTo("must not be blank");
      assertThat(ProblemMessageSourceResolver.of("p", this.violation, "custom").getDefaultMessage())
          .isEqualTo("custom");
      assertThat(ProblemMessageSourceResolver.of("p", this.violation, 400).getDefaultMessage())
          .isEqualTo("400");
    }
  }
}
