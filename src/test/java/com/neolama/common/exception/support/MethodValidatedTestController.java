package com.neolama.common.exception.support;

import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code @Validated} controller: Spring's AOP method validation applies instead of the built-in MVC
 * validation, so a failed parameter constraint raises {@code ConstraintViolationException}.
 */
@RestController
@Validated
@RequestMapping("/method-validated")
class MethodValidatedTestController {

  @GetMapping("/size")
  String size(@RequestParam @Min(1) final int size) {
    return "size " + size;
  }
}
