package com.neolama.common.exception.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoException;
import com.neolama.common.exception.dao.constraint.ConstraintNameResolver;
import com.neolama.common.exception.dao.constraint.MongoConstraintNameResolver;
import com.neolama.common.exception.dao.constraint.PostgresConstraintNameResolver;
import java.sql.SQLException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessException;

/** The lost-race rule shared by the web advice and application retries. */
class ConcurrencyFailureDetectorTest {

  private final ConcurrencyFailureDetector detector =
      new ConcurrencyFailureDetector(
          List.of(new PostgresConstraintNameResolver(), new MongoConstraintNameResolver()));

  @Test
  void nullIsNotConcurrencyFailure() {
    assertThat(this.detector.isConcurrencyFailure(null)).isFalse();
  }

  /** What {@code MongoTemplate} throws on an {@code @Version} mismatch. */
  @Test
  void optimisticLockIsConcurrencyFailure() {
    assertThat(this.detector.isConcurrencyFailure(new OptimisticLockingFailureException("stale")))
        .isTrue();
  }

  @Test
  void pessimisticLockIsConcurrencyFailure() {
    assertThat(this.detector.isConcurrencyFailure(new CannotAcquireLockException("lock timeout")))
        .isTrue();
  }

  /** A job or listener may see the failure wrapped in its own exception. */
  @Test
  void wrappedConcurrencyFailureIsFound() {
    final RuntimeException error =
        new IllegalStateException(
            "job failed", new RuntimeException(new OptimisticLockingFailureException("stale")));
    assertThat(this.detector.isConcurrencyFailure(error)).isTrue();
  }

  /** What Spring Data MongoDB throws when a transaction loses a write conflict. */
  @Test
  void mongoWriteConflictIsConcurrencyFailure() {
    final MongoException cause = new MongoException(112, "WriteConflict");
    assertThat(
            this.detector.isConcurrencyFailure(
                new DataIntegrityViolationException(cause.getMessage(), cause)))
        .isTrue();
  }

  /** Stands in for Spring Data MongoDB's {@code TransientClientSessionException}. */
  @Test
  void mongoTransientTransactionErrorIsConcurrencyFailure() {
    final MongoException cause = new MongoException(251, "NoSuchTransaction");
    cause.addLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL);
    assertThat(this.detector.isConcurrencyFailure(new TransientSessionException(cause))).isTrue();
  }

  /** The commit may have succeeded; retrying the transaction could apply it twice. */
  @Test
  void mongoUnknownCommitResultIsNotConcurrencyFailure() {
    final MongoException cause = new MongoException(50, "MaxTimeMSExpired");
    cause.addLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL);
    assertThat(this.detector.isConcurrencyFailure(new TransientSessionException(cause))).isFalse();
  }

  @Test
  void mongoTransientDuplicateIsNotConcurrencyFailure() {
    final MongoException cause = new MongoException(11000, "E11000");
    cause.addLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL);
    assertThat(this.detector.isConcurrencyFailure(new DuplicateKeyException("dup", cause)))
        .isFalse();
  }

  @Test
  void otherErrorsAreNotConcurrencyFailures() {
    assertThat(this.detector.isConcurrencyFailure(new IllegalStateException("boom"))).isFalse();
    assertThat(
            this.detector.isConcurrencyFailure(
                new DataIntegrityViolationException("x", new SQLException("H2 error"))))
        .isFalse();
    assertThat(
            this.detector.isConcurrencyFailure(
                new TransientSessionException(new MongoException(251, "NoSuchTransaction"))))
        .isFalse();
  }

  @Test
  void loopingCauseChainEnds() {
    final RuntimeException first = new RuntimeException("first");
    final RuntimeException second = new RuntimeException("second", first);
    first.initCause(second);
    assertThat(this.detector.isConcurrencyFailure(first)).isFalse();
  }

  /** Only the first resolver that supports the most specific cause is asked. */
  @Test
  void firstSupportingResolverDecides() {
    final ConcurrencyFailureDetector ordered =
        new ConcurrencyFailureDetector(List.of(new FixedResolver(false), new FixedResolver(true)));
    assertThat(ordered.isConcurrencyFailure(new IllegalStateException("x"))).isFalse();
  }

  @Test
  void withoutResolversOnlySpringConcurrencyFailuresCount() {
    final ConcurrencyFailureDetector bare = new ConcurrencyFailureDetector(List.of());
    assertThat(bare.isConcurrencyFailure(new OptimisticLockingFailureException("stale"))).isTrue();
    assertThat(bare.isConcurrencyFailure(new MongoException(112, "WriteConflict"))).isFalse();
  }

  /** A transient failure that is not a {@code ConcurrencyFailureException}. */
  private static final class TransientSessionException extends TransientDataAccessException {

    TransientSessionException(final Throwable cause) {
      super(cause.getMessage(), cause);
    }
  }

  /** Supports every cause and gives a fixed answer. */
  private record FixedResolver(boolean concurrencyFailure) implements ConstraintNameResolver {

    @Override
    public boolean supports(final Throwable cause) {
      return true;
    }

    @Override
    public @Nullable String resolveConstraintName(final Throwable cause) {
      return null;
    }

    @Override
    public boolean isUniqueViolation(final Throwable cause) {
      return false;
    }

    @Override
    public boolean isConcurrencyFailure(final Throwable cause) {
      return this.concurrencyFailure;
    }
  }
}
