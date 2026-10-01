package com.neolama.oxneer.common.exception.advice.validation;

public interface ValidationAdvice
    extends ConstraintViolationAdvice,
        BindAdvice,
        MethodArgumentNotValidAdvice,
        MethodArgumentTypeMismatchAdvice,
        TypeMismatchAdvice {}
