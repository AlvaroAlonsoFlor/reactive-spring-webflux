package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
class MoviesInfoIntegrationTest {

    private final URI MOVIES_INFO_URL = URI.create("/v1/movies-info");

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var movieInfoList = List.of(new MovieInfo("abc", "Batman Begins", 2005,
                        List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008,
                        List.of("Cristian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo(null, "The Dark Knight", 2012,
                        List.of("Cristian Bale", "Tom Hardie"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfoList).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {

        var movieInfo = new MovieInfo(null, "Batman Begins1", 2005,
                List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri("/v1/movies-info")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void getAllMoviesInfo() {

        webTestClient
                .get()
                .uri("/v1/movies-info")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getAllMoviesInfoByYear() {

        var url = UriComponentsBuilder.fromUri(MOVIES_INFO_URL)
                .queryParam("year", 2005)
                .buildAndExpand()
                .toUri();

        webTestClient
                .get()
                .uri(url)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getAllMovieInfoById() {

        webTestClient
                .get()
                .uri("/v1/movies-info/abc")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                });
    }

    @Test
    void getAllMovieInfoByIdNotFound() {

        webTestClient
                .get()
                .uri("/v1/movies-info/def")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void updateMovieInfo() {

        var movieInfo = new MovieInfo("abc", "Batman Begins1", 2005,
                List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .put()
                .uri("/v1/movies-info/abc")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("Batman Begins1");
    }

    @Test
    void updateMovieInfoNotFound() {

        var movie = new MovieInfo("adf", "Batman Begins1", 2005,
                List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .put()
                .uri("/v1/movies-info/adf")
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfo() {

        webTestClient
                .delete()
                .uri("/v1/movies-info/abc")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getMoviesInfoStream() {

        var movieInfo = new MovieInfo(null, "Batman Begins1", 2005,
                List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri("/v1/movies-info")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });

        var moviesStream = webTestClient
                .get()
                .uri("/v1/movies-info/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();



        StepVerifier.create(moviesStream)
                .assertNext(movieInfoResult -> {
                    assertNotNull(movieInfoResult.getMovieInfoId());
                })
                .thenCancel()
                .verify();
    }
}