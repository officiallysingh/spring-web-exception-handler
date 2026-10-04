package com.neolama.common.exception.advice.dao;

/**
 * Groups the data-access advice traits handled by {@link
 * com.neolama.common.exception.autoconfigure.WebDaoExceptionHandler}.
 */
public interface DaoAdvice extends DataIntegrityViolationAdvice, DuplicateKeyExceptionAdvice {}
