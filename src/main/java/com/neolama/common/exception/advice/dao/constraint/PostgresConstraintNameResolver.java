package com.neolama.common.exception.advice.dao.constraint;

public class PostgresConstraintNameResolver implements ConstraintNameResolver {

  /** {@inheritDoc} */
  @Override
  public String resolveConstraintName(final String exceptionMessage) {
    String exMessage = exceptionMessage.trim();
    try {
      exMessage = exMessage.substring(exMessage.indexOf("constraint") + 12);
      return exMessage.substring(0, exMessage.indexOf("\""));
    } catch (final Exception e) {
      // Ignored on purpose
    }
    return "postgres.duplicate.key";
  }

  @Override
  public Type getType() {
    return Type.POSTGRESQL;
  }
}
