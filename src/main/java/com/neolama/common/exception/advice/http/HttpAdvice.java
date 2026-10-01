package com.neolama.common.exception.advice.http;

public interface HttpAdvice
    extends HttpMediaTypeNotAcceptableAdvice,
        HttpMediaTypeNotSupportedExceptionAdvice,
        UnsupportedMediaTypeStatusAdvice,
        HttpRequestMethodNotSupportedAdvice,
        MethodNotAllowedAdvice,
        NotAcceptableStatusAdvice,
        ResponseStatusAdvice {}
