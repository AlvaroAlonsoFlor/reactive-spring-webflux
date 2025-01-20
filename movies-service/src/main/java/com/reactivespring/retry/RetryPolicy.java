package com.reactivespring.retry;

import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryPolicy {

    public static Retry create(long maxAttempts, int delaySeconds) {
        return Retry.fixedDelay(maxAttempts, Duration.ofSeconds(delaySeconds))
                .filter(ex -> ex instanceof MoviesInfoServerException || ex instanceof ReviewsServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        Exceptions.propagate(retrySignal.failure()));
    }
}
