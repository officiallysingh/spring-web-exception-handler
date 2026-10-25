package com.neolama.common.exception.advice.dao.constraint;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.mongodb.MongoException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * {@link ConstraintNameResolver} for MongoDB driver exceptions.
 *
 * <p>Reads {@code collection.index} from a duplicate-key message such as {@code E11000 duplicate
 * key error collection: db.states index: name_1 dup key: ...}, which gives {@code states.name_1}.
 *
 * @author Rajveer Singh
 */
public class MongoConstraintNameResolver implements ConstraintNameResolver {

  /** Captures the collection (without the database prefix) and the index name. */
  private static final Pattern DUPLICATE_KEY =
      Pattern.compile("collection: [^.\\s]+\\.(\\S+) index: (\\S+)");

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Throwable cause) {
    return cause instanceof MongoException;
  }

  /** {@inheritDoc} */
  @Override
  public @Nullable String resolveConstraintName(final Throwable cause) {
    final String message = cause.getMessage();
    if (message == null) {
      return null;
    }
    final Matcher matcher = DUPLICATE_KEY.matcher(message);
    return matcher.find() ? matcher.group(1) + DOT + matcher.group(2) : null;
  }
}
