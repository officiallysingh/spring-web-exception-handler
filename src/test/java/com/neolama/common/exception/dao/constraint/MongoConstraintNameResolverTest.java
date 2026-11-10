package com.neolama.common.exception.dao.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.MongoException;
import org.junit.jupiter.api.Test;

class MongoConstraintNameResolverTest {

  private final MongoConstraintNameResolver resolver = new MongoConstraintNameResolver();

  @Test
  void duplicateKeyGivesCollectionAndIndex() {
    final MongoException cause =
        new MongoException(
            11000,
            "E11000 duplicate key error collection: db.states index: name_1 dup key: { name: \"x\" }");
    assertThat(this.resolver.resolveConstraintName(cause)).isEqualTo("states.name_1");
  }

  @Test
  void collectionNameWithDotsIsKept() {
    final MongoException cause =
        new MongoException(
            11000,
            "E11000 duplicate key error collection: db.app.users index: email_1 dup key: {}");
    assertThat(this.resolver.resolveConstraintName(cause)).isEqualTo("app.users.email_1");
  }

  @Test
  void nonDuplicateErrorReturnsNull() {
    assertThat(
            this.resolver.resolveConstraintName(
                new MongoException(121, "Document failed validation")))
        .isNull();
  }

  @Test
  void duplicateKeyCodesAreUniqueViolations() {
    assertThat(this.resolver.isUniqueViolation(new MongoException(11000, "E11000"))).isTrue();
    assertThat(this.resolver.isUniqueViolation(new MongoException(11001, "E11001"))).isTrue();
    assertThat(this.resolver.isUniqueViolation(new MongoException(121, "validation"))).isFalse();
    assertThat(this.resolver.isUniqueViolation(new RuntimeException("E11000"))).isFalse();
  }

  @Test
  void writeConflictIsConcurrencyFailure() {
    assertThat(this.resolver.isConcurrencyFailure(new MongoException(112, "WriteConflict")))
        .isTrue();
  }

  @Test
  void transientTransactionErrorIsConcurrencyFailure() {
    final MongoException cause = new MongoException(251, "NoSuchTransaction");
    cause.addLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL);
    assertThat(this.resolver.isConcurrencyFailure(cause)).isTrue();
  }

  /** A retry cannot win a duplicate, even when the server labels it transient. */
  @Test
  void transientDuplicateKeyIsNotConcurrencyFailure() {
    final MongoException cause = new MongoException(11000, "E11000");
    cause.addLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL);
    assertThat(this.resolver.isConcurrencyFailure(cause)).isFalse();
  }

  /** The commit may have succeeded, so retrying the whole transaction could apply it twice. */
  @Test
  void unknownCommitResultIsNotConcurrencyFailure() {
    final MongoException cause = new MongoException(50, "MaxTimeMSExpired");
    cause.addLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL);
    assertThat(this.resolver.isConcurrencyFailure(cause)).isFalse();
    final MongoException writeConflict = new MongoException(112, "WriteConflict");
    writeConflict.addLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL);
    assertThat(this.resolver.isConcurrencyFailure(writeConflict)).isFalse();
  }

  @Test
  void otherErrorsAreNotConcurrencyFailures() {
    assertThat(this.resolver.isConcurrencyFailure(new MongoException(251, "NoSuchTransaction")))
        .isFalse();
    assertThat(this.resolver.isConcurrencyFailure(new MongoException(11000, "E11000"))).isFalse();
    assertThat(this.resolver.isConcurrencyFailure(new RuntimeException("WriteConflict"))).isFalse();
  }

  @Test
  void emptyMessageReturnsNull() {
    assertThat(this.resolver.resolveConstraintName(new MongoException(""))).isNull();
  }
}
