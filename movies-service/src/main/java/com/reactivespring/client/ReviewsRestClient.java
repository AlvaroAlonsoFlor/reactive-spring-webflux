package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import com.reactivespring.retry.RetryPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@Slf4j
public class ReviewsRestClient {

    @Value("${restClient.reviewsUrl}")
    private String reviewUrl;

    private final WebClient webClient;

    @Value("${restClient.retryPolicy.delaySeconds}")
    private int delaySeconds;

    @Value("${restClient.retryPolicy.maxAttempts}")
    private long maxAttempts;

    private final Retry retryPolicy;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
        this.retryPolicy = RetryPolicy.create(maxAttempts, delaySeconds);;
    }

    public Flux<Review> retrieveReviews(String movieId) {
        var url = UriComponentsBuilder.fromUriString(reviewUrl)
                .queryParam("movieId", movieId)
                .buildAndExpand().toUriString();

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.info("Status code is {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsClientException(responseMessage)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.info("Status code is {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsServerException(
                                    "Server Exception in ReviewsService " + responseMessage)));
                })
                .bodyToFlux(Review.class)
                .retryWhen(this.retryPolicy);
    }
}
