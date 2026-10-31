package com.neolama.apps.webmvc;

import java.sql.SQLException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller under test in the {@code @WebMvcTest} slice. */
@RestController
public class WebMvcSliceController {

  @GetMapping("/slice/illegal-state")
  String illegalState() {
    throw new IllegalStateException("boom");
  }

  @GetMapping("/slice/duplicate-key")
  String duplicateKey() {
    throw new DuplicateKeyException("insert failed", new SQLException("boom"));
  }

  @GetMapping("/slice/access-denied")
  String accessDenied() {
    throw new AccessDeniedException("Access is denied");
  }
}
