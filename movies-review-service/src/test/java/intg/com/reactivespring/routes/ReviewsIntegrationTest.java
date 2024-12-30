package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
public class ReviewsIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository repository;

    static final String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review("test-id", 2L, "Excellent Movie", 8.0)
        );

        repository.saveAll(reviewsList).blockLast();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll().block();
    }

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assertNotNull(savedReview.getReviewId());
                });
    }

    @Test
    void getAllReviews() {

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void getAllReviewsByMovieInfoId() {

        webTestClient
                .get()
                .uri(REVIEWS_URL + "?movieInfoId=1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void UpdateReview() {
        var review = new Review("test-id", 2L, "Average Movie", 5.0);

        webTestClient
                .put()
                .uri(REVIEWS_URL + "/test-id")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assertThat(review, is(savedReview));
                });
    }

    @Test
    void deleteReview() {

        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/test-id")
                .exchange()
                .expectStatus()
                .isNoContent();
    }



}
