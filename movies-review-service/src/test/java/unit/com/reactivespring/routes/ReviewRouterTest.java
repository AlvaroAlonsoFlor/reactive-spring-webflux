package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = { ReviewRouter.class, ReviewHandler.class })
@AutoConfigureWebTestClient
public class ReviewRouterTest {

    @MockitoBean
    private ReviewReactiveRepository repository;

    @Autowired
    private WebTestClient webTestClient;

    static final String REVIEWS_URL = "/v1/reviews";

    @Test
    void addReview() {

        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(repository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

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

        when(repository.findAll()).thenReturn(Flux.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(1);
    }

    @Test
    void getAllReviewsByMovieInfoId() {

        when(repository.findReviewsByMovieInfoId(1L)).thenReturn(Flux.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        webTestClient
                .get()
                .uri(REVIEWS_URL + "?movieInfoId=1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(1);
    }

    @Test
    void updateReview() {
        var review = new Review("abc", 2L, "Average Movie", 5.0);

        when(repository.findById("abc")).thenReturn(Mono.just(review));
        when(repository.save(isA(Review.class))).thenReturn(Mono.just(review));

        webTestClient
                .put()
                .uri(REVIEWS_URL + "/abc")
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

        when(repository.findById("abc")).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        when(repository.delete(isA(Review.class))).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/abc")
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
