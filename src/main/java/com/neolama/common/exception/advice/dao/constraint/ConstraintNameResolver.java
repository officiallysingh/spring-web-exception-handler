package com.neolama.common.exception.advice.dao.constraint;

/** Extracts a database constraint name from a persistence exception message. */
public interface ConstraintNameResolver {

  /**
   * Resolves the constraint name from the given exception message.
   *
   * @param exceptionMessage the database exception message
   * @return the extracted constraint name, or a default value if not found
   */
  String resolveConstraintName(final String exceptionMessage);

  /**
   * Returns the database this resolver supports.
   *
   * @return the resolver type
   */
  Type getType();

  /** Database whose constraint names this resolver can extract. */
  enum Type {
    /** PostgreSQL constraint messages. */
    POSTGRESQL,

    /** MongoDB write-error messages. */
    MONGO_DB
  }
}
