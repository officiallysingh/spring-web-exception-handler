package com.neolama.common.exception.support;

import com.mongodb.MongoException;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
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

  /** Hibernate under JPA reports a unique violation as a plain data-integrity violation. */
  @GetMapping("/duplicate-postgres-jpa")
  String duplicatePostgresJpa() {
    throw new DataIntegrityViolationException(
        "could not execute statement",
        new PSQLException(
            "ERROR: duplicate key value violates unique constraint \"employees_name_key\"",
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

  /** What {@code MongoTemplate} throws on an {@code @Version} mismatch. */
  @GetMapping("/optimistic-lock")
  String optimisticLock() {
    throw new OptimisticLockingFailureException(
        "Cannot save entity 42 with version 3 to collection employees; Has it been modified"
            + " meanwhile");
  }

  /** Stands in for JPA's {@code ObjectOptimisticLockingFailureException}, a subclass. */
  @GetMapping("/optimistic-lock-jpa")
  String optimisticLockJpa() {
    throw new JpaStyleOptimisticLockingFailureException("Row was updated by another transaction");
  }

  @GetMapping("/lock-timeout")
  String lockTimeout() {
    throw new CannotAcquireLockException(
        "could not obtain lock",
        new PSQLException(
            "ERROR: canceling statement due to lock timeout", PSQLState.UNKNOWN_STATE));
  }

  @GetMapping("/concurrency-failure")
  String concurrencyFailure() {
    throw new ConcurrencyFailureException("concurrent update");
  }

  /** A subclass of {@link OptimisticLockingFailureException}, as JPA throws. */
  static final class JpaStyleOptimisticLockingFailureException
      extends OptimisticLockingFailureException {

    JpaStyleOptimisticLockingFailureException(final String message) {
      super(message);
    }
  }
}
