package com.neolama.oxneer.common.exception.advice.io;

public interface IOAdvice
    extends MessageNotReadableAdvice,
        MaxUploadSizeExceededExceptionAdvice,
        DataBufferLimitExceptionAdvice,
        MultipartAdvice {}
