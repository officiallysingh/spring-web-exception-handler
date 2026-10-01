package com.neolama.common.exception.advice.dao;

import com.neolama.common.exception.advice.Exceptional;

public interface BaseDataIntegrityAdvice extends Exceptional {

  /**
   * Resolves the constraint name from the exception message.
   *
   * @param exceptionMessage the exception message
   * @return the resolved constraint name
   */
  String resolveConstraintName(final String exceptionMessage);
}
