package com.neolama.common.exception.advice.routing;

public interface RoutingAdvice
    extends MissingServletRequestParameterAdvice,
        MissingServletRequestPartAdvice,
        MissingRequestHeaderAdvice,
        NoHandlerFoundAdvice,
        ServletRequestBindingAdvice {}
