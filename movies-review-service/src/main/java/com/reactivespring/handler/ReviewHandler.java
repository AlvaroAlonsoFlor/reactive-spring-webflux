package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    Sinks.Many<Review> reviewsSink = Sinks.many().replay().latest();

    @Autowired
    private Validator validator;

    private final ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .doOnNext(review -> reviewsSink.tryEmitNext(review))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("constraintViolations: {}", constraintViolations);

        if (!constraintViolations.isEmpty()) {
            var errorMessage = constraintViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));

            throw new ReviewDataException(errorMessage);
        }

    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieInfoId = request.queryParam("movieInfoId");
        var reviews = movieInfoId.isPresent()
                ? reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()))
                : reviewReactiveRepository.findAll();

        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var id = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(id)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for id: " + id)));

        return existingReview.flatMap(review -> request.bodyToMono(Review.class)
                .map(reqReview -> {
                    review.setComment(reqReview.getComment());
                    review.setRating(reqReview.getRating());
                    return review;
                }).flatMap(reviewReactiveRepository::save)
                .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview)));
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var id = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(id);

        return existingReview.flatMap(reviewReactiveRepository::delete)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(), Review.class)
                .log();
    }
}
