package com.neolama.common.exception.support;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

/** Endpoints for the HTTP advices: methods, media types, and response-status exceptions. */
@RestController
@RequestMapping("/http")
class HttpTestController {

  @GetMapping("/get-only")
  String getOnly() {
    return "ok";
  }

  @PostMapping(value = "/json-body", consumes = MediaType.APPLICATION_JSON_VALUE)
  String jsonBody(@RequestBody final Map<String, Object> body) {
    return "ok";
  }

  @GetMapping(value = "/json-only", produces = MediaType.APPLICATION_JSON_VALUE)
  String jsonOnly() {
    return "{}";
  }

  @GetMapping("/status-reason")
  String statusReason() {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee 7 not found");
  }

  @GetMapping("/status-no-reason")
  String statusNoReason() {
    throw new ResponseStatusException(HttpStatus.GONE);
  }

  @GetMapping("/status-499")
  String status499() {
    throw new ResponseStatusException(HttpStatusCode.valueOf(499));
  }

  @GetMapping("/status-599")
  String status599() {
    throw new ResponseStatusException(HttpStatusCode.valueOf(599));
  }

  @GetMapping("/webflux-method-not-allowed")
  String webfluxMethodNotAllowed() {
    throw new MethodNotAllowedException(HttpMethod.POST, Set.of(HttpMethod.GET));
  }

  @GetMapping("/no-methods")
  String noMethods() throws HttpRequestMethodNotSupportedException {
    throw new HttpRequestMethodNotSupportedException("POST");
  }

  @GetMapping("/webflux-no-methods")
  String webfluxNoMethods() {
    throw new MethodNotAllowedException(HttpMethod.POST, Set.of());
  }

  @GetMapping("/webflux-no-media-types")
  String webfluxNoMediaTypes() {
    throw new UnsupportedMediaTypeStatusException(MediaType.TEXT_PLAIN, List.of());
  }

  @GetMapping("/webflux-not-acceptable")
  String webfluxNotAcceptable() {
    throw new NotAcceptableStatusException(List.of(MediaType.APPLICATION_JSON));
  }

  @GetMapping("/webflux-not-acceptable-reason")
  String webfluxNotAcceptableReason() {
    throw new NotAcceptableStatusException("No JSON here");
  }

  @GetMapping("/webflux-unsupported-media-type")
  String webfluxUnsupportedMediaType() {
    throw new UnsupportedMediaTypeStatusException(
        MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON));
  }
}
