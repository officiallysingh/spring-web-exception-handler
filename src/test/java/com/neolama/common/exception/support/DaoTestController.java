package com.neolama.common.exception.support;

import com.mongodb.MongoException;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Throws data-access exceptions as Spring would translate them from the drivers. */
@RestController
@RequestMapping("/dao")
class DaoTestController {

  @GetMapping("/duplicate-postgres")
  String duplicatePostgres() {
    throw new DuplicateKeyException(
        "insert failed",
        new PSQLException(
            "ERROR: duplicate key value violates unique constraint \"employees_name_key\"",
            PSQLState.UNIQUE_VIOLATION));
  }

  @GetMapping("/duplicate-postgres-unconfigured")
  String duplicatePostgresUnconfigured() {
    throw new DuplicateKeyException(
        "insert failed",
        new PSQLException(
            "ERROR: duplicate key value violates unique constraint \"orders_ref_key\"\n"
                + "  Detail: Key (ref)=(secret-ref) already exists.",
            PSQLState.UNIQUE_VIOLATION));
  }

  @GetMapping("/not-null-postgres")
  String notNullPostgres() {
    throw new DataIntegrityViolationException(
        "insert failed",
        new PSQLException(
            "ERROR: null value in column \"name\" of relation \"employees\" violates not-null"
                + " constraint",
            PSQLState.NOT_NULL_VIOLATION));
  }

  @GetMapping("/duplicate-mongo")
  String duplicateMongo() {
    throw new DuplicateKeyException(
        "insert failed",
        new MongoException(
            11000,
            "E11000 duplicate key error collection: db.states index: name_1 dup key: { name: \"x\" }"));
  }

  @GetMapping("/null-message")
  String nullMessage() {
    throw new DataIntegrityViolationException("insert failed", new SQLException((String) null));
  }
}
