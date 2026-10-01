package com.neolama.oxneer.common.exception.advice.http;

public interface HttpAdvice
    extends HttpMediaTypeNotAcceptableAdvice,
        HttpMediaTypeNotSupportedExceptionAdvice,
        UnsupportedMediaTypeStatusAdvice,
        HttpRequestMethodNotSupportedAdvice,
        MethodNotAllowedAdvice,
        NotAcceptableStatusAdvice,
        ResponseStatusAdvice {}
