package com.neolama.common.exception.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class DefaultProblem implements ErrorType {

  private final String errorKey;

  private final String defaultDetail;

  private final HttpStatus status;
}
