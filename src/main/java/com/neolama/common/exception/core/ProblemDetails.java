package com.neolama.common.exception.core;

import static com.neolama.common.exception.core.ProblemConstant.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@JsonPropertyOrder({
  "type",
  "status",
  "instance",
  METHOD_KEY,
  CODE_KEY,
  "title",
  "detail",
  TIMESTAMP_KEY,
  VIOLATIONS_KEY,
  ERRORS_KEY
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProblemDetails extends ProblemDetail {

  private static final String TYPE_URL = "http://localhost:8090/";

  private String code;

  @Setter private HttpMethod method;

  private OffsetDateTime timestamp;

  private List<Problem> violations;

  private List<Problem> errors;

  public static ProblemDetails of(
      final String errorKey, @Nullable final String defaultDetail, final HttpStatus status) {
    return of(errorKey, defaultDetail, null, status);
  }

  public static ProblemDetails of(
      final String errorKey,
      @Nullable final String defaultDetail,
      @Nullable Object[] detailArgs,
      final HttpStatus status) {
    final String defaultCode =
        errorKey.contains(".")
            ? ProblemUtils.getProblemCode(status)
            : ProblemUtils.getProblemCode(errorKey);
    final String code = ProblemMessageProvider.getMessage(CODE_CODE_PREFIX + errorKey, defaultCode);
    final String title =
        ProblemMessageProvider.getMessage(TITLE_CODE_PREFIX + errorKey, status.getReasonPhrase());
    final String detail =
        ProblemMessageProvider.getMessage(
            DETAIL_CODE_PREFIX + errorKey,
            StringUtils.isNotBlank(defaultDetail) ? defaultDetail : DEFAULT_DETAIL,
            detailArgs);
    return of(status, code, title, detail);
  }

  public static ProblemDetails of(final HttpStatus status) {
    return of(
        status, String.valueOf(status.value()), status.getReasonPhrase(), status.getReasonPhrase());
  }

  public static ProblemDetails of(
      final HttpStatus status, final String code, final String title, final String detail) {
    ProblemDetails problemDetails = new ProblemDetails();
    problemDetails.setStatus(status);
    problemDetails.setTitle(title);
    problemDetails.setDetail(detail);
    problemDetails.code = code;
    problemDetails.timestamp = OffsetDateTime.now(ZoneId.of("UTC"));
    return problemDetails;
  }

  public void setViolations(final Collection<Problem> violations) {
    this.violations = violations.stream().toList();
  }

  public void setErrors(final Collection<Problem> errors) {
    this.errors = errors.stream().toList();
  }
}
