package com.neolama.common.exception.advice.security;

/**
 * Groups the Spring Security advice traits handled by {@link
 * com.neolama.common.exception.autoconfigure.WebSecurityExceptionHandler}.
 */
public interface SecurityAdvice
    extends UsernameNotFoundAdvice,
        BadCredentialsExceptionAdvice,
        AuthenticationExceptionAdvice,
        InsufficientAuthenticationAdvice,
        AccessDeniedExceptionAdvice {}
