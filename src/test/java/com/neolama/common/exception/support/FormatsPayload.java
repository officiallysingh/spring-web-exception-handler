package com.neolama.common.exception.support;

import java.util.List;
import java.util.Map;

/** Request body whose values fail to parse at different places of the JSON path. */
public record FormatsPayload(
    Integer count, FormatsItem item, List<Integer> numbers, Map<String, Integer> counts) {}
