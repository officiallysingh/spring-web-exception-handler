package com.neolama.common.exception.advice.general;

/**
 * Groups the general advice traits handled by {@link
 * com.neolama.common.exception.autoconfigure.WebExceptionHandler}.
 */
public interface GeneralAdvice
    extends ThrowableProblemAdvice, ThrowableAdvice, UnsupportedOperationAdvice {}
