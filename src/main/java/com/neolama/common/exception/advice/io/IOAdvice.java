package com.neolama.common.exception.advice.io;

/**
 * Groups the I/O advice traits handled by {@link
 * com.neolama.common.exception.autoconfigure.WebExceptionHandler}.
 */
public interface IOAdvice
    extends MessageNotReadableAdvice,
        MaxUploadSizeExceededExceptionAdvice,
        DataBufferLimitExceptionAdvice,
        MultipartAdvice {}
