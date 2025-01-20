package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(properties = {
        "restClient.moviesInfoUrl=http://localhost:8084/v1/movies-info",
        "restClient.reviewsUrl=http://localhost:8084/v1/reviews",
        "restClient.retryPolicy.maxAttempts=1"
})
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webClient;

    @Test
    public void retrieveMoviesById() {

        var id = "abc";

        stubFor(get(urlEqualTo(String.format("/v1/movies-info/%s", id)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movies-info.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        webClient
                .get()
                .uri("/v1/movies/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(response -> {
                    var movie = response.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });
    }

    @Test
    public void retrieveMoviesByIdNotFoundMoviesInfo() {

        var id = "abc";

        stubFor(get(urlEqualTo(String.format("/v1/movies-info/%s", id)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        webClient
                .get()
                .uri("/v1/movies/{id}", id)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void retrieveMoviesByIdNotFoundReview() {

        var id = "abc";

        stubFor(get(urlEqualTo(String.format("/v1/movies-info/%s", id)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movies-info.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        webClient
                .get()
                .uri("/v1/movies/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(response -> {
                    var movie = response.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().isEmpty();
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });
    }

    @Test
    public void retrieveMoviesById5XXMovieInfo() {

        var id = "abc";
        var path = String.format("/v1/movies-info/%s", id);

        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Movie Info Service Unavailable")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        webClient
                .get()
                .uri("/v1/movies/{id}", id)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Internal Server Error");

        verify(1, getRequestedFor(urlPathEqualTo(path)));
    }
}
