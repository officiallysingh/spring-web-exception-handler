package com.neolama.common.exception.advice.dao.constraint;

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
  void emptyMessageReturnsNull() {
    assertThat(this.resolver.resolveConstraintName(new MongoException(""))).isNull();
  }
}
