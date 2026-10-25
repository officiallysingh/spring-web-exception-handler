package com.neolama.common.exception.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body with a required name and an optional nick of 2 to 5 characters. */
public record ValidatedPayload(@NotBlank String name, @Size(min = 2, max = 5) String nick) {}
