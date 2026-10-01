package com.neolama.oxneer.common.exception.advice.dao.constraint;

public interface ConstraintNameResolver {

  /**
   * Resolves the constraint name from the given exception message.
   *
   * @param exceptionMessage the database exception message
   * @return the extracted constraint name, or a default value if not found
   */
  String resolveConstraintName(final String exceptionMessage);

  Type getType();

  enum Type {
    POSTGRESQL,
    MONGO_DB
  }
}
