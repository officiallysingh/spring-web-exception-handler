package com.neolama.common.exception.support;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Throws Spring Security exceptions from a controller, so the MVC advices handle them. */
@RestController
@RequestMapping("/security")
class SecurityTestController {

  @GetMapping("/access-denied")
  String accessDenied() {
    throw new AccessDeniedException("Access is denied");
  }

  @GetMapping("/authentication")
  String authentication() {
    throw new AuthenticationCredentialsNotFoundException("No credentials");
  }

  @GetMapping("/bad-credentials")
  String badCredentials() {
    throw new BadCredentialsException("Bad credentials");
  }

  @GetMapping("/insufficient")
  String insufficient() {
    throw new InsufficientAuthenticationException("Full authentication is required");
  }

  @GetMapping("/username-not-found")
  String usernameNotFound() {
    throw new UsernameNotFoundException("User alice not found");
  }
}
