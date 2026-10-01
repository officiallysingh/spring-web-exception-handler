package com.neolama.common.exception.core;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

import jakarta.annotation.Nullable;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@UtilityClass
public class ProblemUtils {

  /**
   * Returns the status code as a string for the given {@link HttpStatusCode}.
   *
   * @param httpStatus the HTTP status
   * @return the status code
   */
  public static String statusCode(final HttpStatusCode httpStatus) {
    return String.valueOf(httpStatus.value());
  }

  //  public static ThrowableProblem toProblem(final Throwable throwable) {
  //    final HttpStatus status = resolveStatus(throwable);
  //    ThrowableProblem problem =
  // Problems.newInstance(status).detail(throwable.getMessage()).build();
  //    problem.setStackTrace(createStackTrace(throwable));
  //    return problem;
  //  }

  /**
   * Resolves the {@link HttpStatus} for the given {@link Throwable}.
   *
   * @param throwable the throwable
   * @return the resolved status, defaults to {@link HttpStatus#INTERNAL_SERVER_ERROR}
   */
  public static Optional<HttpStatus> resolveStatus(final Throwable throwable) {
    return Optional.ofNullable(ProblemUtils.resolveResponseStatus(throwable))
        .map(ResponseStatus::value);
  }

  /**
   * Resolves the {@link ResponseStatus} annotation for the given {@link Throwable}.
   *
   * @param type the throwable type
   * @return the response status annotation, or {@code null} if not found
   */
  public static ResponseStatus resolveResponseStatus(final Throwable type) {

    @Nullable
    final ResponseStatus candidate = findMergedAnnotation(type.getClass(), ResponseStatus.class);
    return candidate == null && type.getCause() != null
        ? resolveResponseStatus(type.getCause())
        : candidate;
  }

  /**
   * Creates a stack trace for the given {@link Throwable}, potentially filtering out trailing
   * partial sublists if cause chains are enabled.
   *
   * @param throwable the throwable
   * @return the stack trace
   */
  public static StackTraceElement[] createStackTrace(final Throwable throwable) {
    final Throwable cause = throwable.getCause();
    //    if (cause == null || !ProblemBeanRegistry.problemProperties().isCauseChainsEnabled()) {
    final StackTraceElement[] stackTrace = throwable.getStackTrace();
    //    } else {
    //
    //      final StackTraceElement[] next = cause.getStackTrace();
    //      final StackTraceElement[] current = throwable.getStackTrace();
    //
    //      final int length =
    //          current.length - Lists.lengthOfTrailingPartialSubList(asList(next),
    // asList(current));
    //      final StackTraceElement[] stackTrace = new StackTraceElement[length];
    //      System.arraycopy(current, 0, stackTrace, 0, length);
    //      return stackTrace;
    //    }
    return stackTrace;
  }

  /**
   * Returns the stack trace of the given exception as a single line string.
   *
   * @param exception the exception
   * @return the single line stack trace
   */
  public static String getStackTrace(final Throwable exception) {
    String stacktrace = ExceptionUtils.getStackTrace(exception);
    StringBuilder escapedStacktrace = new StringBuilder(stacktrace.length() + 100);
    StringCharacterIterator scitr = new StringCharacterIterator(stacktrace);

    char current = scitr.first();
    // DONE = \\uffff (not a character)
    String lastAppend = null;
    while (current != CharacterIterator.DONE) {
      if (current == '\t' || current == '\r' || current == '\n') {
        if (!" ".equals(lastAppend)) {
          escapedStacktrace.append(" ");
          lastAppend = " ";
        }
      } else {
        // nothing matched - just text as it is.
        escapedStacktrace.append(current);
        lastAppend = "" + current;
      }
      current = scitr.next();
    }
    return escapedStacktrace.toString();
  }

  public static String getExceptionCode(String value) {
    return value
        .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
        .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
        .toLowerCase(Locale.ROOT)
        .replaceFirst("-exception$", "");
  }
}
