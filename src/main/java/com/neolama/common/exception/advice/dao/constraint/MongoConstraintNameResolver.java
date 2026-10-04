package com.neolama.common.exception.advice.dao.constraint;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

/**
 * {@link ConstraintNameResolver} implementation for MongoDB.
 *
 * @author Rajveer Singh
 */
public class MongoConstraintNameResolver implements ConstraintNameResolver {

  /** {@inheritDoc} */
  @Override
  public String resolveConstraintName(final String exceptionMessage) {
    String exMessage = exceptionMessage.trim();
    try {
      String temp = exMessage.substring(exMessage.indexOf("collection: ") + 12);
      String collectionName = temp.substring(temp.indexOf(".") + 1, temp.indexOf(" index: "));
      temp = exMessage.substring(exMessage.indexOf("index: ") + 7);
      String indexName = temp.substring(0, temp.indexOf(" "));
      return collectionName + DOT + indexName;
    } catch (final Exception e) {
      // Ignored on purpose
    }
    return "mongo.duplicate.key";
  }

  /** {@inheritDoc} */
  @Override
  public Type getType() {
    return Type.MONGO_DB;
  }
}
