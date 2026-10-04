package com.neolama.common.exception.core;

import static com.neolama.common.exception.core.ProblemConstant.*;

import jakarta.annotation.Nullable;
import java.util.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

/**
 * Fluent factory for {@link ThrowableProblem} instances.
 *
 * <p>Callers start with {@link #of(String)}, {@link #of(ErrorType)}, {@link #of(Collection)}, or
 * {@link #of(Problem, Problem...)} and finish with {@link ProblemBuildable#throwAble()} or {@link
 * ProblemBuildable#throwAble(HttpStatus)}.
 */
@Slf4j
@UtilityClass
public class Problems {

  // ----------- Builder -----------

  /**
   * Starts a builder for the given error key.
   *
   * @param errorKey the message-source key used to resolve problem text
   * @return the next step in the builder
   */
  public static DefaultDetailBuilder of(final String errorKey) {
    return new Builder(errorKey);
  }

  /**
   * Starts a builder for the given error type.
   *
   * @param errorType the error type that supplies the key, detail, and status
   * @return the next step in the builder
   */
  public static DetailArgsBuilder of(final ErrorType errorType) {
    return new Builder(errorType);
  }

  /**
   * Starts a builder for the given problems.
   *
   * @param problems the problems stored on the response
   * @return the next step in the builder
   */
  public static CauseBuilder of(final Collection<Problem> problems) {
    Assert.notEmpty(problems, "'problems' must not ne null or empty");
    Assert.noNullElements(problems, "'problems' must not contain null elements");
    return new Builder(problems);
  }

  /**
   * Starts a builder for the given problem and any further problems.
   *
   * @param problem the first problem
   * @param others additional problems
   * @return the next step in the builder
   */
  public static CauseBuilder of(final Problem problem, final Problem... others) {
    Assert.notNull(problem, "'problem' must not be null");
    Assert.noNullElements(others, "'others' must not contain null elements");
    List<Problem> probs = new ArrayList<>();
    probs.add(problem);
    if (ArrayUtils.isNotEmpty(others)) {
      probs.addAll(Arrays.asList(others));
    }
    return new Builder(probs);
  }

  /** Builder step for setting the default detail. */
  public interface DefaultDetailBuilder extends DetailArgsBuilder {
    /**
     * Sets the default detail.
     *
     * @param detail the default detail
     * @return the next step in the builder
     */
    DetailArgsBuilder defaultDetail(@Nullable final String detail);
  }

  /** Builder step for setting message arguments and the cause. */
  public interface DetailArgsBuilder extends CauseBuilder {

    /**
     * Sets arguments substituted into the resolved detail message.
     *
     * @param args the message arguments, or {@code null} when the detail has none
     * @return the next step in the builder
     */
    CauseBuilder detailArgs(@Nullable final Object... args);
  }

  /** Builder step for setting the cause of the exception. */
  public interface CauseBuilder extends ParameterBuilder {

    /**
     * Sets the cause of the exception.
     *
     * @param cause the cause, or {@code null} when there is none
     * @return the next step in the builder
     */
    ParameterBuilder cause(@Nullable final Throwable cause);
  }

  /** Builder step for setting additional parameters. */
  public interface ParameterBuilder extends ParametersBuilder {
    /**
     * Adds an additional parameter.
     *
     * @param key the parameter key
     * @param value the parameter value
     * @return this builder
     */
    ParameterBuilder parameter(final String key, final Object value);
  }

  /** Builder step for setting multiple parameters at once. */
  public interface ParametersBuilder extends ProblemBuildable {
    /**
     * Adds multiple additional parameters.
     *
     * @param parameters the parameters
     * @return this builder
     */
    ProblemBuildable parameters(@Nullable final Map<String, Object> parameters);
  }

  /** Final builder step that creates the exception. */
  public interface ProblemBuildable {

    /**
     * Creates the exception using the error type status, or {@link
     * HttpStatus#INTERNAL_SERVER_ERROR} when no error type was supplied.
     *
     * @return the assembled exception
     */
    ThrowableProblem throwAble();

    /**
     * Creates the exception using the given HTTP status.
     *
     * @param status the HTTP status of the problem
     * @return the assembled exception
     */
    ThrowableProblem throwAble(final HttpStatus status);
  }

  /**
   * Assembles a {@link ThrowableProblem} from an error key, {@link ErrorType}, or collection of
   * {@link Problem}s.
   */
  public static class Builder implements DefaultDetailBuilder {

    private static final Set<String> RESERVED_PROPERTIES =
        new HashSet<>(
            Arrays.asList(
                "type",
                "status",
                "instance",
                METHOD_KEY,
                CODE_KEY,
                "title",
                "detail",
                TIMESTAMP_KEY,
                VIOLATIONS_KEY,
                ERRORS_KEY));

    private ErrorType errorType;
    private String errorKey;
    private String defaultDetail;
    private Collection<Problem> problems;
    private Object[] detailsArgs;
    private Throwable cause;
    private HttpStatus status;

    private final Map<String, Object> parameters = new LinkedHashMap<>();

    /**
     * Starts a builder for the given error type.
     *
     * @param errorType the error type that supplies the key, detail, and status
     */
    Builder(final ErrorType errorType) {
      this.errorType = errorType;
      this.status = errorType.getStatus();
    }

    /**
     * Starts a builder for the given error key.
     *
     * @param errorKey the message-source key used to resolve problem text
     */
    Builder(final String errorKey) {
      Assert.hasText(errorKey, "'errorKey' must not be null or empty");
      this.errorKey = errorKey;
    }

    /**
     * Starts a builder for the given nested problems.
     *
     * @param problems the problems stored on the response
     */
    Builder(final Collection<Problem> problems) {
      this.problems = problems;
      this.status = HttpStatus.MULTI_STATUS;
    }

    /** {@inheritDoc} */
    @Override
    public DetailArgsBuilder defaultDetail(@Nullable String detail) {
      this.defaultDetail = detail;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public CauseBuilder detailArgs(final @Nullable Object... args) {
      this.detailsArgs = args;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public ParameterBuilder cause(final @Nullable Throwable cause) {
      this.cause = cause;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public ParameterBuilder parameter(final String key, final Object value) {
      Assert.hasLength(key, "'key' must not be null or empty");
      Assert.isTrue(!RESERVED_PROPERTIES.contains(key), "Property " + key + " is reserved");
      this.parameters.put(key, value);
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProblemBuildable parameters(@Nullable final Map<String, Object> parameters) {
      if (MapUtils.isNotEmpty(parameters)) {
        parameters.forEach(this::parameter);
      }
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public ThrowableProblem throwAble() {
      this.status =
          Objects.nonNull(this.errorType)
              ? this.errorType.getStatus()
              : HttpStatus.INTERNAL_SERVER_ERROR;
      return this.build();
    }

    /** {@inheritDoc} */
    @Override
    public ThrowableProblem throwAble(final HttpStatus status) {
      this.status = status;
      return this.build();
    }

    /**
     * Builds the exception from the values collected by this builder.
     *
     * @return the assembled exception
     */
    private ThrowableProblem build() {
      ProblemDetails problemDetail;
      if (Objects.nonNull(this.errorType)) {
        problemDetail =
            ProblemDetails.of(
                this.errorType.getErrorKey(),
                this.errorType.getDefaultDetail(),
                this.detailsArgs,
                this.status);
      } else if (StringUtils.isNotBlank(this.errorKey)) {
        problemDetail =
            ProblemDetails.of(this.errorKey, this.defaultDetail, this.detailsArgs, this.status);
      } else if (CollectionUtils.isNotEmpty(this.problems)) {
        problemDetail = ProblemDetails.of(this.status);
        problemDetail.setErrors(this.problems);
      } else {
        problemDetail = ProblemDetails.of(this.status);
      }
      if (MapUtils.isNotEmpty(this.parameters)) {
        problemDetail.setProperties(this.parameters);
      }
      return ThrowableProblem.of(problemDetail, this.cause);
    }
  }
}
