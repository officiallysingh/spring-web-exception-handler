package com.neolama.common.exception.core;

import static com.neolama.common.exception.core.ProblemConstant.STATUS_CODE_PREFIX;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

import com.neolama.common.exception.autoconfigure.ProblemMessageProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Helpers for HTTP status lookup, number parsing, size formatting, and problem codes. */
@Slf4j
@UtilityClass
public class ProblemUtils {

  /** A valid error key: a letter, then letters, digits, dots, hyphens, or underscores. */
  private static final Pattern ERROR_KEY = Pattern.compile("\\p{Alpha}[\\p{Alnum}._-]*");

  /** Splits camel case: the position before an upper-case letter that follows any character. */
  private static final Pattern CAMEL_CASE_BOUNDARY = Pattern.compile("(?<=.)(?=\\p{Upper})");

  /** A run of characters that are neither letters nor digits. */
  private static final Pattern SEPARATORS = Pattern.compile("[^\\p{L}\\p{N}]+");

  /** Matches a run of decimal digits. */
  private static final Pattern NUMBER = Pattern.compile("\\d+");

  /** Units used by {@link #formatSize(long)}, each 1024 times the previous one. */
  private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};

  /** Bytes in a kilobyte, as used by {@link #formatSize(long)}. */
  private static final BigDecimal KILO = BigDecimal.valueOf(1024);

  /**
   * Reads the first {@code status.{errorKey}} entry configured for the given error keys.
   *
   * <p>The keys are tried in order, most specific first. A value that is not a known HTTP status
   * code is ignored, with a single warn line naming the entry, and the next key is tried.
   *
   * @param errorKeys the error keys, most specific first
   * @return the configured status, or empty when no key has a known status code
   */
  public static Optional<HttpStatus> configuredStatus(final String... errorKeys) {
    for (final String errorKey : errorKeys) {
      final Optional<HttpStatus> status = configuredStatus(errorKey);
      if (status.isPresent()) {
        return status;
      }
    }
    return Optional.empty();
  }

  /**
   * Reads the {@code status.{errorKey}} entry from the message source.
   *
   * <p>A value that is not a known HTTP status code is ignored, with a single warn line naming the
   * entry.
   *
   * @param errorKey the error key
   * @return the configured status, or empty when the entry is missing or not a known status code
   */
  public static Optional<HttpStatus> configuredStatus(final String errorKey) {
    final String value =
        ProblemMessageProvider.getMessage(STATUS_CODE_PREFIX + errorKey, StringUtils.EMPTY).trim();
    if (value.isEmpty()) {
      return Optional.empty();
    }
    final HttpStatus status =
        StringUtils.isNumeric(value) ? HttpStatus.resolve(NumberUtils.toInt(value, -1)) : null;
    if (status == null) {
      log.warn("Ignoring {}{}={}: not an HTTP status code", STATUS_CODE_PREFIX, errorKey, value);
    }
    return Optional.ofNullable(status);
  }

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
  public static @Nullable ResponseStatus resolveResponseStatus(final Throwable type) {
    final ResponseStatus candidate = findMergedAnnotation(type.getClass(), ResponseStatus.class);
    return candidate == null && type.getCause() != null
        ? resolveResponseStatus(type.getCause())
        : candidate;
  }

  /**
   * Returns the first whole number in the given text, such as {@code 1024} in {@code "Part exceeded
   * the disk usage limit of 1024 bytes"}.
   *
   * @param text the text to search, may be {@code null}
   * @param defaultValue the value returned when the text has no number or it does not fit a {@code
   *     long}
   * @return the first number in the text, or {@code defaultValue}
   */
  public static long firstNumber(final @Nullable String text, final long defaultValue) {
    if (text == null) {
      return defaultValue;
    }
    final Matcher matcher = NUMBER.matcher(text);
    if (!matcher.find()) {
      return defaultValue;
    }
    try {
      return Long.parseLong(matcher.group());
    } catch (final NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Returns the last whole number in the given text, such as {@code 1048576} in {@code "The field
   * file1 exceeds its maximum permitted size of 1048576 bytes."}.
   *
   * @param text the text to search, may be {@code null}
   * @param defaultValue the value returned when the text has no number or it does not fit a {@code
   *     long}
   * @return the last number in the text, or {@code defaultValue}
   */
  public static long lastNumber(final @Nullable String text, final long defaultValue) {
    if (text == null) {
      return defaultValue;
    }
    final Matcher matcher = NUMBER.matcher(text);
    String last = null;
    while (matcher.find()) {
      last = matcher.group();
    }
    if (last == null) {
      return defaultValue;
    }
    try {
      return Long.parseLong(last);
    } catch (final NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Formats a byte count for people, using binary units: {@code 512 B}, {@code 1.5 KB}, {@code 10
   * MB}.
   *
   * <p>Values below 1 KB stay in bytes. Larger values are shown in the biggest unit that keeps them
   * at 1 or more, with at most two decimals.
   *
   * @param bytes the size in bytes, not negative
   * @return the formatted size
   */
  public static String formatSize(final long bytes) {
    int unit = 0;
    BigDecimal value = BigDecimal.valueOf(bytes);
    while (unit < SIZE_UNITS.length - 1 && value.compareTo(KILO) >= 0) {
      value = value.divide(KILO, 2, RoundingMode.HALF_UP);
      unit++;
    }
    return value.stripTrailingZeros().toPlainString() + " " + SIZE_UNITS[unit];
  }

  /**
   * Converts an error key into a lower-case, hyphen-separated problem code.
   *
   * <p>Every upper-case letter starts a new segment, and every run of other characters than letters
   * and digits, such as dots, underscores, or spaces, becomes one hyphen: {@code
   * NullPointerException} becomes {@code null-pointer-exception}, {@code IOException} becomes
   * {@code i-o-exception}, and {@code row.invalid} becomes {@code row-invalid}.
   *
   * <p>Never fails, because it also converts keys the library builds from class, constraint, and
   * parameter names. A key with no letter or digit, such as {@code !!!}, is returned trimmed.
   *
   * @param errorKey the error key to convert, typically an exception simple class name
   * @return the problem code
   */
  public static String getProblemCode(final String errorKey) {
    final String split = CAMEL_CASE_BOUNDARY.matcher(errorKey).replaceAll("-");
    final String hyphenated = SEPARATORS.matcher(split.toLowerCase(Locale.ROOT)).replaceAll("-");
    final String code = StringUtils.strip(hyphenated, "-");
    return code.isEmpty() ? errorKey.trim() : code;
  }

  /**
   * Checks the format of an error key supplied by application code.
   *
   * <p>A valid key starts with a letter and contains only letters, digits, dots, hyphens, and
   * underscores, such as {@code order.not.found}, {@code order-not-found}, or {@code
   * OrderNotFound}. Checking when the problem is built reports a wrong key while the code is
   * written, rather than turning it into a confusing response.
   *
   * @param errorKey the error key to check, may be {@code null}
   * @return the error key
   * @throws IllegalArgumentException if the key is {@code null} or not in the expected format
   */
  public static String requireValidErrorKey(final @Nullable String errorKey) {
    if (errorKey == null || !ERROR_KEY.matcher(errorKey).matches()) {
      throw new IllegalArgumentException(
          "Invalid error key '"
              + errorKey
              + "': an error key must start with a letter and contain only letters, digits,"
              + " dots, hyphens, or underscores, such as 'order.not.found', 'order-not-found', or"
              + " 'OrderNotFound'");
    }
    return errorKey;
  }

  /**
   * Converts an HTTP status name into a lower-case, hyphen-separated problem code.
   *
   * @param status the HTTP status
   * @return the problem code
   */
  public static String getProblemCode(final HttpStatus status) {
    return status.name().toLowerCase().replace('_', '-');
  }
}
