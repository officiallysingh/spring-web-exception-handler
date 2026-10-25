package com.neolama.common.exception.support;

import com.neolama.common.exception.core.Problem;
import com.neolama.common.exception.core.Problems;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

/** Throws exceptions for the general advices: the fallback, thrown problems, unsupported. */
@RestController
@RequestMapping("/general")
class GeneralTestController {

  @GetMapping("/illegal-state")
  String illegalState() {
    throw new IllegalStateException("db password is x");
  }

  @GetMapping("/conflict")
  String conflict() {
    throw new ConflictException("Order 7 already exists");
  }

  @GetMapping("/wrapped-conflict")
  String wrappedConflict() {
    throw new IllegalStateException("wrapper", new ConflictException("Order 7 already exists"));
  }

  @GetMapping("/async-timeout")
  String asyncTimeout() {
    throw new AsyncRequestTimeoutException();
  }

  @GetMapping("/configured-status")
  String configuredStatus() {
    throw new ConfiguredStatusException("configured");
  }

  @GetMapping("/invalid-status")
  String invalidStatus() {
    throw new InvalidStatusException("invalid");
  }

  @GetMapping("/unsupported")
  String unsupported() {
    throw new UnsupportedOperationException("not yet");
  }

  @GetMapping("/problem")
  String problem() {
    throw Problems.of(TestErrors.ORDER_NOT_FOUND).detailArgs(42).throwAble();
  }

  @GetMapping("/problem-with-parameters")
  String problemWithParameters() {
    throw Problems.of("sample.problem")
        .defaultDetail("Sample detail")
        .parameter("orderId", 42)
        .throwAble(HttpStatus.BAD_REQUEST);
  }

  @GetMapping("/problems")
  String problems() {
    throw Problems.of(
            Problem.of("a-1", "First", "first detail"),
            Problem.of("b-2", "Second", "second detail"))
        .throwAble();
  }

  @GetMapping("/problem-not-found")
  String problemNotFound() {
    throw Problems.of("order.not.found").throwAble(HttpStatus.NOT_FOUND);
  }

  @GetMapping("/spaced-code")
  String spacedCode() {
    throw Problems.of("spaced.code").throwAble(HttpStatus.BAD_REQUEST);
  }

  @GetMapping("/localized")
  String localized() {
    throw Problems.of("localized.problem").throwAble(HttpStatus.BAD_REQUEST);
  }
}
