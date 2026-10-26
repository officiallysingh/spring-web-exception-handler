package com.neolama.common.exception.support;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.beans.PropertyChangeEvent;
import java.time.LocalDate;
import org.springframework.beans.TypeMismatchException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints for the validation advices. */
@RestController
@RequestMapping("/validation")
class ValidationTestController {

  @PostMapping("/body")
  String body(@Valid @RequestBody final ValidatedPayload payload) {
    return payload.name();
  }

  @GetMapping("/page")
  String page(@RequestParam @Min(0) final int page, @RequestParam @Min(1) final int size) {
    return page + "/" + size;
  }

  @GetMapping("/number")
  String number(@RequestParam final int count) {
    return "count " + count;
  }

  @GetMapping("/renamed")
  String renamed(@RequestParam("joined_after") final LocalDate joinedAfter) {
    return "joined after " + joinedAfter;
  }

  @GetMapping("/renamed-configured")
  String renamedConfigured(@RequestParam("joined_before") final LocalDate joinedBefore) {
    return "joined before " + joinedBefore;
  }

  @GetMapping("/bind")
  String bind() throws BindException {
    final BeanPropertyBindingResult result =
        new BeanPropertyBindingResult(new ValidatedPayload(null, null), "validatedPayload");
    result.rejectValue("name", "NotBlank", "must not be blank");
    result.reject("payload.invalid", "payload is invalid");
    throw new BindException(result);
  }

  @GetMapping("/type-mismatch")
  String typeMismatch() {
    throw new TypeMismatchException(
        new PropertyChangeEvent(this, "count", null, "abc"), Integer.class);
  }
}
