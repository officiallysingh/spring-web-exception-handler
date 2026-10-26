package com.neolama.common.exception.core;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import java.util.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

/**
 * Fluent factory for {@link ThrowableProblem} instances.
 *
 * <p>Callers start with {@link #of(String)}, {@link #of(ErrorType)}, {@link #of(Collection)}, or
 * {@link #of(Problem, Problem...)} and finish with {@link ProblemBuildable#throwAble()} or {@link
 * ProblemBuildable#throwAble(HttpStatus)}.
 *
 * <p>Build and throw a new {@link ThrowableProblem} for every failure; do not store the result of
 * {@code throwAble()} and throw it again. See {@link ThrowableProblem} for why.
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
    Assert.notEmpty(problems, "'problems' must not be null or empty");
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
    DetailArgsBuilder defaultDetail(final @Nullable String detail);
  }

  /** Builder step for setting message arguments and the cause. */
  public interface DetailArgsBuilder extends CauseBuilder {

    /**
     * Sets arguments substituted into the resolved detail message.
     *
     * @param args the message arguments, or {@code null} when the detail has none
     * @return the next step in the builder
     */
    CauseBuilder detailArgs(final Object @Nullable ... args);
  }

  /** Builder step for setting the cause of the exception. */
  public interface CauseBuilder extends ParameterBuilder {

    /**
     * Sets the cause of the exception.
     *
     * @param cause the cause, or {@code null} when there is none
     * @return the next step in the builder
     */
    ParameterBuilder cause(final @Nullable Throwable cause);
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
    ProblemBuildable parameters(final @Nullable Map<String, Object> parameters);
  }

  /** Final builder step that creates the exception. */
  public interface ProblemBuildable {

    /**
     * Creates the exception with a status taken from the {@code status.{errorKey}} message, then
     * from the {@link ErrorType}, then {@link HttpStatus#INTERNAL_SERVER_ERROR}. A collection of
     * problems uses {@link HttpStatus#INTERNAL_SERVER_ERROR}.
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

    private @Nullable ErrorType errorType;
    private @Nullable String errorKey;
    private @Nullable String defaultDetail;
    private Collection<Problem> problems = List.of();
    private Object @Nullable [] detailsArgs;
    private @Nullable Throwable cause;

    private final Map<String, Object> parameters = new LinkedHashMap<>();

    /**
     * Starts a builder for the given error type.
     *
     * @param errorType the error type that supplies the key, detail, and status
     */
    Builder(final ErrorType errorType) {
      Assert.notNull(errorType, "'errorType' must not be null");
      Assert.hasText(errorType.getErrorKey(), "'errorType.errorKey' must not be null or empty");
      this.errorType = errorType;
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
    }

    /** {@inheritDoc} */
    @Override
    public DetailArgsBuilder defaultDetail(final @Nullable String detail) {
      this.defaultDetail = detail;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public CauseBuilder detailArgs(final Object @Nullable ... args) {
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
    public ProblemBuildable parameters(final @Nullable Map<String, Object> parameters) {
      MapUtils.emptyIfNull(parameters).forEach(this::parameter);
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public ThrowableProblem throwAble() {
      return this.build(this.resolveStatus());
    }

    /** {@inheritDoc} */
    @Override
    public ThrowableProblem throwAble(final HttpStatus status) {
      Assert.notNull(status, "'status' must not be null");
      return this.build(status);
    }

    /**
     * Resolves the status used by {@link #throwAble()}.
     *
     * <p>For an error key or {@link ErrorType}, a {@code status.{errorKey}} entry in the message
     * source wins, then {@link ErrorType#getStatus()}, then {@link
     * HttpStatus#INTERNAL_SERVER_ERROR}. A collection of problems always uses {@link
     * HttpStatus#INTERNAL_SERVER_ERROR}.
     *
     * @return the resolved status
     */
    private HttpStatus resolveStatus() {
      if (Objects.nonNull(this.errorType)) {
        final HttpStatus javaDefault =
            Objects.requireNonNullElse(
                this.errorType.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
        return configuredStatus(this.errorType.getErrorKey()).orElse(javaDefault);
      } else if (Objects.nonNull(this.errorKey)) {
        return configuredStatus(this.errorKey).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        // Initialized with problems: no single error key to look up.
        return HttpStatus.INTERNAL_SERVER_ERROR;
      }
    }

    /**
     * Reads the {@code status.{errorKey}} entry from the message source.
     *
     * @param errorKey the error key
     * @return the configured status, or empty when the entry is missing or not a known status code
     */
    private static Optional<HttpStatus> configuredStatus(final String errorKey) {
      final String value =
          ProblemMessageProvider.getMessage(STATUS_CODE_PREFIX + errorKey, StringUtils.EMPTY);
      if (!StringUtils.isNumeric(StringUtils.trimToEmpty(value))) {
        return Optional.empty();
      }
      return Optional.ofNullable(HttpStatus.resolve(Integer.parseInt(value.trim())));
    }

    /**
     * Builds the exception from the values collected by this builder.
     *
     * @param status the HTTP status of the problem
     * @return the assembled exception
     */
    private ThrowableProblem build(final HttpStatus status) {
      ProblemDetails problemDetail;
      if (Objects.nonNull(this.errorType)) {
        problemDetail =
            ProblemDetails.of(
                this.errorType.getErrorKey(),
                this.errorType.getDefaultDetail(),
                this.detailsArgs,
                status);
      } else if (Objects.nonNull(this.errorKey)) {
        problemDetail =
            ProblemDetails.of(this.errorKey, this.defaultDetail, this.detailsArgs, status);
      } else {
        // Initialized with problems.
        problemDetail = ProblemDetails.of(status);
        problemDetail.setErrors(this.problems);
      }
      if (MapUtils.isNotEmpty(this.parameters)) {
        problemDetail.setProperties(this.parameters);
      }
      return ThrowableProblem.of(problemDetail, this.cause);
    }
  }
}
