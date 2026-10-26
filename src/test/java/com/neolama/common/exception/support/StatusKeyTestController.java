package com.neolama.common.exception.support;

import java.beans.PropertyChangeEvent;
import java.time.LocalDate;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Endpoints whose error keys have a {@code status.} entry of their own in the test bundle. The
 * names are used nowhere else, so the entries affect no other test.
 */
@RestController
@RequestMapping("/status-key")
class StatusKeyTestController {

  @GetMapping("/param")
  String param(@RequestParam final int limit) {
    return "limit " + limit;
  }

  @GetMapping("/header")
  String header(@RequestHeader("X-Region") final String region) {
    return region;
  }

  @PostMapping("/part")
  String part(@RequestPart("avatar") final MultipartFile avatar) {
    return avatar.getName();
  }

  @GetMapping("/typed")
  String typed(@RequestParam("since") final LocalDate since) {
    return "since " + since;
  }

  @GetMapping("/property")
  String property() {
    throw new TypeMismatchException(
        new PropertyChangeEvent(this, "weight", null, "heavy"), Integer.class);
  }

  @PostMapping("/body")
  String body(@RequestBody final StatusKeyPayload payload) {
    return "age " + payload.age();
  }
}
