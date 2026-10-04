package com.neolama.common.exception.advice.routing;

/**
 * Groups the request-routing advice traits handled by {@link
 * com.neolama.common.exception.autoconfigure.WebExceptionHandler}.
 */
public interface RoutingAdvice
    extends MissingServletRequestParameterAdvice,
        MissingServletRequestPartAdvice,
        MissingRequestHeaderAdvice,
        NoHandlerFoundAdvice,
        ServletRequestBindingAdvice {}
