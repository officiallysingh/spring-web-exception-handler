package com.neolama.oxneer.common.exception.core;

import static com.neolama.oxneer.common.exception.core.ProblemConstant.*;

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

@Slf4j
@UtilityClass
public class Problems {

  // ----------- Builder -----------
  public static DefaultDetailBuilder of(final String errorKey) {
    return new Builder(errorKey);
  }

  public static DetailArgsBuilder of(final ErrorType errorType) {
    return new Builder(errorType);
  }

  public static CauseBuilder of(final Collection<Problem> problems) {
    Assert.notEmpty(problems, "'problems' must not ne null or empty");
    Assert.noNullElements(problems, "'problems' must not contain null elements");
    return new Builder(problems);
  }

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

  public interface DetailArgsBuilder extends CauseBuilder {

    CauseBuilder detailArgs(@Nullable final Object... args);
  }

  public interface CauseBuilder extends ParameterBuilder {

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

  public interface ProblemBuildable {

    ThrowableProblem throwAble();

    ThrowableProblem throwAble(final HttpStatus status);
  }

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

    Builder(final ErrorType errorType) {
      this.errorType = errorType;
      this.status = errorType.getStatus();
    }

    Builder(final String errorKey) {
      Assert.hasText(errorKey, "'errorKey' must not be null or empty");
      this.errorKey = errorKey;
    }

    Builder(final Collection<Problem> problems) {
      this.problems = problems;
      this.status = HttpStatus.MULTI_STATUS;
    }

    @Override
    public DetailArgsBuilder defaultDetail(@Nullable String detail) {
      this.defaultDetail = detail;
      return this;
    }

    @Override
    public CauseBuilder detailArgs(final @Nullable Object... args) {
      this.detailsArgs = args;
      return this;
    }

    @Override
    public ParameterBuilder cause(final @Nullable Throwable cause) {
      this.cause = cause;
      return this;
    }

    @Override
    public ParameterBuilder parameter(final String key, final Object value) {
      Assert.hasLength(key, "'key' must not be null or empty");
      Assert.isTrue(!RESERVED_PROPERTIES.contains(key), "Property " + key + " is reserved");
      this.parameters.put(key, value);
      return this;
    }

    @Override
    public ProblemBuildable parameters(@Nullable final Map<String, Object> parameters) {
      if (MapUtils.isNotEmpty(parameters)) {
        parameters.forEach(this::parameter);
      }
      return this;
    }

    @Override
    public ThrowableProblem throwAble() {
      this.status =
          Objects.nonNull(this.errorType)
              ? this.errorType.getStatus()
              : HttpStatus.INTERNAL_SERVER_ERROR;
      return this.build();
    }

    @Override
    public ThrowableProblem throwAble(final HttpStatus status) {
      this.status = status;
      return this.build();
    }

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
