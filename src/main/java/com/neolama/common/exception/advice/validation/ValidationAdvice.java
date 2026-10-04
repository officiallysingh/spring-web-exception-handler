package com.neolama.common.exception.advice.validation;

/**
 * Groups the validation advice traits handled by {@link
 * com.neolama.common.exception.autoconfigure.WebExceptionHandler}.
 */
public interface ValidationAdvice
    extends ConstraintViolationAdvice,
        BindAdvice,
        MethodArgumentNotValidAdvice,
        MethodArgumentTypeMismatchAdvice,
        TypeMismatchAdvice {}
