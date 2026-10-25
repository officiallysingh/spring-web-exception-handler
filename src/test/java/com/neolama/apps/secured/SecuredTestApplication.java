package com.neolama.apps.secured;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Secured application wired as the README describes: the library's entry point and access-denied
 * handler are set on the filter chain, so security failures outside controllers become problems.
 */
@SpringBootApplication
public class SecuredTestApplication {

  /** Test-only user name. */
  public static final String USER = "user";

  /** Test-only password for {@link #USER}. */
  public static final String PASSWORD = "user-password";

  @Bean
  SecurityFilterChain securityFilterChain(
      final HttpSecurity http,
      final AuthenticationEntryPoint authenticationEntryPoint,
      final AccessDeniedHandler accessDeniedHandler) {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/secured/admin")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
        .exceptionHandling(
            handling ->
                handling
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .csrf(csrf -> csrf.disable())
        .build();
  }

  @Bean
  UserDetailsService users() {
    return new InMemoryUserDetailsManager(
        User.withUsername(USER).password("{noop}" + PASSWORD).roles("USER").build());
  }

  /** Endpoints behind the filter chain. */
  @RestController
  static class SecuredController {

    @GetMapping("/secured/hello")
    String hello() {
      return "hello";
    }

    @GetMapping("/secured/admin")
    String admin() {
      return "admin";
    }
  }
}
