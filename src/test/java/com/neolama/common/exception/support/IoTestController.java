package com.neolama.common.exception.support;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/** Endpoints for the I/O advices: unreadable bodies, size limits, multipart failures. */
@RestController
@RequestMapping("/io")
class IoTestController {

  @PostMapping("/formats")
  String formats(@RequestBody final FormatsPayload payload) {
    return "ok";
  }

  @GetMapping("/data-buffer-limit")
  String dataBufferLimit() {
    throw new DataBufferLimitException("Exceeded limit on max bytes to buffer : 262144");
  }

  @GetMapping("/data-buffer-limit-unknown")
  String dataBufferLimitUnknown() {
    throw new DataBufferLimitException("Too much data");
  }

  @GetMapping("/max-upload-size")
  String maxUploadSize() {
    throw new MaxUploadSizeExceededException(1048576);
  }

  @GetMapping("/max-upload-size-unknown")
  String maxUploadSizeUnknown() {
    throw new MaxUploadSizeExceededException(-1);
  }

  @GetMapping("/upload-file-size-tomcat")
  String uploadFileSizeTomcat() {
    throw new MaxUploadSizeExceededException(
        -1,
        new IllegalStateException(
            new FileSizeLimitExceededException(
                "The field file1 exceeds its maximum permitted size of 1048576 bytes.",
                2000000,
                1048576)));
  }

  @GetMapping("/upload-request-size-tomcat")
  String uploadRequestSizeTomcat() {
    throw new MaxUploadSizeExceededException(
        -1,
        new IllegalStateException(
            new SizeLimitExceededException(
                "the request was rejected because its size (2000000) exceeds the configured"
                    + " maximum (1048576)",
                2000000,
                1048576)));
  }

  @GetMapping("/multipart")
  String multipart() {
    throw new MultipartException("Failed to parse multipart servlet request");
  }
}
