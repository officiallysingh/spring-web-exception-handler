package com.neolama.common.exception.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProblemConstant {

  public static final String CODE_KEY = "code";
  public static final String METHOD_KEY = "method";
  public static final String TIMESTAMP_KEY = "timestamp";
  public static final String ERRORS_KEY = "errors";
  public static final String VIOLATIONS_KEY = "violations";
  public static final String DOT = ".";

  public static final String CODE_CODE_PREFIX = "code.";
  public static final String TITLE_CODE_PREFIX = "title.";
  public static final String DETAIL_CODE_PREFIX = "detail.";
  public static final String STATUS_CODE_PREFIX = "status.";
  public static final String MESSAGE_CODE_PREFIX = "message.";

  public static final String DEFAULT_CODE = "500";
  public static final String DEFAULT_TITLE = "Internal Server Error";
  public static final String DEFAULT_DETAIL = "An unexpected error occurred";
  public static final String DEFAULT_STATUS = "500";
}
