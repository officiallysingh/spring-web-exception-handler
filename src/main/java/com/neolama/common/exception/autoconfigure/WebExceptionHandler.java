package com.neolama.common.exception.autoconfigure;

import com.neolama.common.exception.advice.general.GeneralAdvice;
import com.neolama.common.exception.advice.http.HttpAdvice;
import com.neolama.common.exception.advice.io.IOAdvice;
import com.neolama.common.exception.advice.routing.RoutingAdvice;
import com.neolama.common.exception.advice.validation.ValidationAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice that turns general, HTTP, I/O, routing, and validation failures into problem
 * details.
 *
 * <p>Enabled in servlet web applications.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RestControllerAdvice
public class WebExceptionHandler
    implements GeneralAdvice, HttpAdvice, IOAdvice, RoutingAdvice, ValidationAdvice {}
