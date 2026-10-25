package com.neolama.common.exception.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProblemUtilsTest {

  @Test
  void firstNumberReturnsDefaultForNull() {
    assertThat(ProblemUtils.firstNumber(null, -1)).isEqualTo(-1);
  }

  @Test
  void firstNumberReturnsDefaultWhenTextHasNoDigits() {
    assertThat(ProblemUtils.firstNumber("no size here", -1)).isEqualTo(-1);
  }

  @Test
  void firstNumberReadsSingleNumber() {
    assertThat(ProblemUtils.firstNumber("Part exceeded the disk usage limit of 1024 bytes", -1))
        .isEqualTo(1024);
  }

  @Test
  void firstNumberReadsOnlyTheFirstOfSeveralNumbers() {
    assertThat(ProblemUtils.firstNumber("limit of 1024 bytes, part 3", -1)).isEqualTo(1024);
  }

  @Test
  void firstNumberReturnsDefaultWhenNumberDoesNotFitLong() {
    assertThat(ProblemUtils.firstNumber("size 99999999999999999999", -1)).isEqualTo(-1);
  }

  @Test
  void problemCodeHyphenatesCamelCaseClassName() {
    assertThat(ProblemUtils.getProblemCode("NullPointerException"))
        .isEqualTo("null-pointer-exception");
  }

  @Test
  void problemCodeSplitsEveryUpperCaseLetter() {
    assertThat(ProblemUtils.getProblemCode("IOException")).isEqualTo("i-o-exception");
  }

  @Test
  void lastNumberReturnsDefaultForNullOrNoDigits() {
    assertThat(ProblemUtils.lastNumber(null, -1)).isEqualTo(-1);
    assertThat(ProblemUtils.lastNumber("no size here", -1)).isEqualTo(-1);
  }

  @Test
  void lastNumberReadsSingleNumber() {
    assertThat(ProblemUtils.lastNumber("limit of 1024 bytes", -1)).isEqualTo(1024);
  }

  @Test
  void lastNumberSkipsDigitsInFieldNameAndActualSize() {
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
  void formatSizeKeepsBytesBelowOneKilobyte() {
    assertThat(ProblemUtils.formatSize(0)).isEqualTo("0 B");
    assertThat(ProblemUtils.formatSize(512)).isEqualTo("512 B");
    assertThat(ProblemUtils.formatSize(1023)).isEqualTo("1023 B");
  }

  @Test
  void formatSizeUsesLargestFittingUnit() {
    assertThat(ProblemUtils.formatSize(1024)).isEqualTo("1 KB");
    assertThat(ProblemUtils.formatSize(1536)).isEqualTo("1.5 KB");
    assertThat(ProblemUtils.formatSize(1048576)).isEqualTo("1 MB");
    assertThat(ProblemUtils.formatSize(10485760)).isEqualTo("10 MB");
    assertThat(ProblemUtils.formatSize(2_684_354_560L)).isEqualTo("2.5 GB");
  }
}
