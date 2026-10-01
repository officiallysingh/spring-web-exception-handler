package com.neolama.oxneer.common.exception.autoconfigure;

import com.neolama.oxneer.common.exception.advice.general.GeneralAdvice;
import com.neolama.oxneer.common.exception.advice.http.HttpAdvice;
import com.neolama.oxneer.common.exception.advice.io.IOAdvice;
import com.neolama.oxneer.common.exception.advice.routing.RoutingAdvice;
import com.neolama.oxneer.common.exception.advice.validation.ValidationAdvice;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Configuration
@RestControllerAdvice
public class WebExceptionHandler
    implements GeneralAdvice, HttpAdvice, IOAdvice, RoutingAdvice, ValidationAdvice {}
