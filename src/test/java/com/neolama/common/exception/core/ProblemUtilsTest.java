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
}
