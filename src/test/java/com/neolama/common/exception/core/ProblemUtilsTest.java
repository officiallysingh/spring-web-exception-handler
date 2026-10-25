package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.neolama.common.exception.support.ConflictException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ProblemUtilsTest {

  @Nested
  class ResolveStatus {

    @Test
    void readsResponseStatusAnnotation() {
      assertThat(ProblemUtils.resolveStatus(new ConflictException("x")))
          .contains(HttpStatus.CONFLICT);
    }

    @Test
    void readsAnnotationFromCause() {
      assertThat(ProblemUtils.resolveStatus(new IllegalStateException(new ConflictException("x"))))
          .contains(HttpStatus.CONFLICT);
    }

    @Test
    void isEmptyWithoutAnnotation() {
      assertThat(ProblemUtils.resolveStatus(new IllegalStateException("x"))).isEmpty();
      assertThat(ProblemUtils.resolveResponseStatus(new IllegalStateException("x"))).isNull();
    }
  }

  @Nested
  class ProblemCode {

    @Test
    void hyphenatesEveryUpperCaseLetterAndKeepsSuffix() {
      assertThat(ProblemUtils.getProblemCode("NullPointerException"))
          .isEqualTo("null-pointer-exception");
      assertThat(ProblemUtils.getProblemCode("IOException")).isEqualTo("i-o-exception");
      assertThat(ProblemUtils.getProblemCode("OrderNotFound")).isEqualTo("order-not-found");
    }

    @Test
    void usesStatusNameForStatus() {
      assertThat(ProblemUtils.getProblemCode(HttpStatus.INTERNAL_SERVER_ERROR))
          .isEqualTo("internal-server-error");
      assertThat(ProblemUtils.getProblemCode(HttpStatus.NOT_FOUND)).isEqualTo("not-found");
    }
  }

  @Nested
  class FirstNumber {

    @Test
    void returnsDefaultForNullOrNoDigits() {
      assertThat(ProblemUtils.firstNumber(null, -1)).isEqualTo(-1);
      assertThat(ProblemUtils.firstNumber("no size here", -1)).isEqualTo(-1);
    }

    @Test
    void readsOnlyTheFirstNumber() {
      assertThat(ProblemUtils.firstNumber("limit of 1024 bytes, part 3", -1)).isEqualTo(1024);
    }

    @Test
    void returnsDefaultWhenNumberDoesNotFitLong() {
      assertThat(ProblemUtils.firstNumber("size 99999999999999999999", -1)).isEqualTo(-1);
    }
  }

  @Nested
  class LastNumber {

    @Test
    void returnsDefaultForNullOrNoDigits() {
      assertThat(ProblemUtils.lastNumber(null, -1)).isEqualTo(-1);
      assertThat(ProblemUtils.lastNumber("no size here", -1)).isEqualTo(-1);
    }

    @Test
    void skipsDigitsInFieldNameAndActualSize() {
      assertThat(
              ProblemUtils.lastNumber(
                  "The field file1 exceeds its maximum permitted size of 1048576 bytes.", -1))
          .isEqualTo(1048576);
      assertThat(
              ProblemUtils.lastNumber(
                  "the request was rejected because its size (2000000) exceeds the configured"
                      + " maximum (1048576)",
                  -1))
          .isEqualTo(1048576);
    }

    @Test
    void returnsDefaultWhenNumberDoesNotFitLong() {
      assertThat(ProblemUtils.lastNumber("size 99999999999999999999", -1)).isEqualTo(-1);
    }
  }

  @Nested
  class FormatSize {

    @Test
    void keepsBytesBelowOneKilobyte() {
      assertThat(ProblemUtils.formatSize(0)).isEqualTo("0 B");
      assertThat(ProblemUtils.formatSize(512)).isEqualTo("512 B");
      assertThat(ProblemUtils.formatSize(1023)).isEqualTo("1023 B");
    }

    @Test
    void usesLargestFittingUnit() {
      assertThat(ProblemUtils.formatSize(1024)).isEqualTo("1 KB");
      assertThat(ProblemUtils.formatSize(1536)).isEqualTo("1.5 KB");
      assertThat(ProblemUtils.formatSize(1048576)).isEqualTo("1 MB");
      assertThat(ProblemUtils.formatSize(10485760)).isEqualTo("10 MB");
      assertThat(ProblemUtils.formatSize(2_684_354_560L)).isEqualTo("2.5 GB");
      assertThat(ProblemUtils.formatSize(1L << 40)).isEqualTo("1 TB");
    }

    @Test
    void staysInTerabytesBeyondTheLargestUnit() {
      assertThat(ProblemUtils.formatSize(2048L << 40)).isEqualTo("2048 TB");
    }
  }
}
