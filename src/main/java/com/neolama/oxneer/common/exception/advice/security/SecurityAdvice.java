package com.neolama.oxneer.common.exception.advice.security;

public interface SecurityAdvice
    extends UsernameNotFoundAdvice,
        BadCredentialsExceptionAdvice,
        AuthenticationExceptionAdvice,
        InsufficientAuthenticationAdvice,
        AccessDeniedExceptionAdvice {}
