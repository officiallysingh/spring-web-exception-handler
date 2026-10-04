package com.neolama.common.exception.advice.http;

/**
 * Groups the HTTP advice traits handled by {@link
 * com.neolama.common.exception.autoconfigure.WebExceptionHandler}.
 */
public interface HttpAdvice
    extends HttpMediaTypeNotAcceptableAdvice,
        HttpMediaTypeNotSupportedExceptionAdvice,
        UnsupportedMediaTypeStatusAdvice,
        HttpRequestMethodNotSupportedAdvice,
        MethodNotAllowedAdvice,
        NotAcceptableStatusAdvice,
        ResponseStatusAdvice {}
