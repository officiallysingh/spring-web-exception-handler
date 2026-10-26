package com.neolama.common.exception.support;

/** Request body for the status-key tests; {@code age} fails to parse when sent as text. */
public record StatusKeyPayload(Integer age) {}
