package com.neolama.common.exception.dao.constraint;

import static com.neolama.common.exception.core.ProblemConstant.DOT;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * {@link ConstraintNameResolver} for MongoDB driver exceptions.
 *
 * <p>Reads {@code collection.index} from a duplicate-key message such as {@code E11000 duplicate
 * key error collection: db.states index: name_1 dup key: ...}, which gives {@code states.name_1}. A
 * unique violation is recognised by its error code, in the driver's {@link
 * ErrorCategory#DUPLICATE_KEY} category. A concurrency failure is a {@code WriteConflict} (code
 * {@code 112}) or any error the server labels {@code TransientTransactionError}, meaning the whole
 * transaction can be retried. A duplicate key is never one, since a retry cannot win it, and
 * neither is an error labelled {@code UnknownTransactionCommitResult}: the commit may have
 * succeeded, so retrying the whole transaction could apply it twice.
 *
 * @author Rajveer Singh
 */
public class MongoConstraintNameResolver implements ConstraintNameResolver {

  /** Captures the collection (without the database prefix) and the index name. */
  private static final Pattern DUPLICATE_KEY =
      Pattern.compile("collection: [^.\\s]+\\.(\\S+) index: (\\S+)");

  /** The server's {@code WriteConflict} error code. */
  private static final int WRITE_CONFLICT = 112;

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Throwable cause) {
    return cause instanceof MongoException;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUniqueViolation(final Throwable cause) {
    return cause instanceof MongoException exception
        && ErrorCategory.fromErrorCode(exception.getCode()) == ErrorCategory.DUPLICATE_KEY;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isConcurrencyFailure(final Throwable cause) {
    return cause instanceof MongoException exception
        && !isUniqueViolation(exception)
        && !exception.hasErrorLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)
        && (exception.getCode() == WRITE_CONFLICT
            || exception.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL));
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
