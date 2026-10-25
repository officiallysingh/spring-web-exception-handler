package com.neolama.common.exception.support;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Endpoints for the routing advices: missing parameters, headers, parts, and cookies. */
@RestController
@RequestMapping("/routing")
class RoutingTestController {

  @GetMapping("/param")
  String param(@RequestParam final int page) {
    return "page " + page;
  }

  @GetMapping("/header")
  String header(@RequestHeader("X-Tenant") final String tenant) {
    return tenant;
  }

  @PostMapping("/part")
  String part(@RequestPart("file") final MultipartFile file) {
    return file.getName();
  }

  @GetMapping("/cookie")
  String cookie(@CookieValue("session") final String session) {
    return session;
  }
}
