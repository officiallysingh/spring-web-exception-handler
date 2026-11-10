package com.neolama.common.exception.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mongodb.MongoException;
import com.neolama.common.exception.autoconfigure.DataAccessConfiguration;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.resilience.retry.MethodRetryPredicate;

/** The README's {@code @Retryable} predicate, in a context without a web layer. */
class RetryablePredicateTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(DataAccessConfiguration.class))
          .withUserConfiguration(ResilienceConfiguration.class)
          .withBean(Writer.class);

  @Test
  void lostRaceIsRetried() {
    this.runner.run(
        context -> {
          final Writer writer = context.getBean(Writer.class);
          writer.write(2, new MongoException(112, "WriteConflict"));
          assertThat(writer.attempts()).isEqualTo(3);
        });
  }

  @Test
  void otherFailureIsNotRetried() {
    this.runner.run(
        context -> {
          final Writer writer = context.getBean(Writer.class);
          assertThatThrownBy(() -> writer.write(2, new MongoException(121, "validation")))
              .isInstanceOf(DataIntegrityViolationException.class);
          assertThat(writer.attempts()).isEqualTo(1);
        });
  }

  @Configuration(proxyBeanMethods = false)
  @EnableResilientMethods
  static class ResilienceConfiguration {}

  /** Retries exactly the errors the detector calls lost races. */
  static class LostRacePredicate implements MethodRetryPredicate {

    private final ConcurrencyFailureDetector detector;

    LostRacePredicate(final ConcurrencyFailureDetector detector) {
      this.detector = detector;
    }

    @Override
    public boolean shouldRetry(final Method method, final Throwable throwable) {
      return this.detector.isConcurrencyFailure(throwable);
    }
  }

  /** Fails a given number of times as Spring Data MongoDB would, then succeeds. */
  static class Writer {

    private final AtomicInteger attempts = new AtomicInteger();

    public int attempts() {
      return this.attempts.get();
    }

    @Retryable(predicate = LostRacePredicate.class, maxRetries = 3, delay = 0)
    public void write(final int failures, final MongoException cause) {
      if (this.attempts.incrementAndGet() <= failures) {
        throw new DataIntegrityViolationException(cause.getMessage(), cause);
      }
    }
  }
}
