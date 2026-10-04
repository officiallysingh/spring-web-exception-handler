package com.neolama.common.exception.autoconfigure;

import com.neolama.common.exception.advice.general.GeneralAdvice;
import com.neolama.common.exception.advice.http.HttpAdvice;
import com.neolama.common.exception.advice.io.IOAdvice;
import com.neolama.common.exception.advice.routing.RoutingAdvice;
import com.neolama.common.exception.advice.validation.ValidationAdvice;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice that turns general, HTTP, I/O, routing, and validation failures into problem
 * details.
 */
@Configuration
@RestControllerAdvice
public class WebExceptionHandler
    implements GeneralAdvice, HttpAdvice, IOAdvice, RoutingAdvice, ValidationAdvice {}
