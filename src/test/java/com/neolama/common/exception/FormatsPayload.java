package com.neolama.common.exception;

import java.util.List;
import java.util.Map;

/** Request body whose fields fail to parse in different places of the JSON path. */
public record FormatsPayload(
    Integer count, FormatsItem item, List<Integer> numbers, Map<String, Integer> counts) {}
