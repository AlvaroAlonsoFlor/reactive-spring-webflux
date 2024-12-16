package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MoviesInfoService serviceMock;

    private List<MovieInfo> movieInfoList;

    @BeforeEach
    public void setUp() {
        movieInfoList = List.of(new MovieInfo("abc", "Batman Begins", 2005,
                        List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008,
                        List.of("Cristian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo(null, "The Dark Knight", 2012,
                        List.of("Cristian Bale", "Tom Hardie"), LocalDate.parse("2012-07-20")));
    }

    @Test
    void getAllMoviesInfo() {

        when(serviceMock.getAllMoviesInfo()).thenReturn(Flux.fromIterable(movieInfoList));

        webClient
                .get()
                .uri("/v1/movies-info")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMoviesInfoById() {

        var id = "abc";
        var movie = movieInfoList.get(0);

        when(serviceMock.getMovieInfoById(id)).thenReturn(Mono.just(movie));

        webClient
                .get()
                .uri("/v1/movies-info/{1}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertTrue(new ReflectionEquals(savedMovieInfo).matches(movie));
                });
    }

    @Test
    void addMoviesInfo() {
        var movie = new MovieInfo(null, "Batman Begins1", 2005,
                List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(serviceMock.addMovieInfo(movie)).thenReturn(Mono.just(movie));

        webClient
                .post()
                .uri("/v1/movies-info")
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertTrue(new ReflectionEquals(savedMovieInfo).matches(movie));
                });
    }

    @Test
    void addMoviesInfoFailedValidation() {
        var movie = new MovieInfo(null, null, -2005,
                List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(serviceMock.addMovieInfo(movie)).thenReturn(Mono.just(movie));

        webClient
                .post()
                .uri("/v1/movies-info")
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(result -> {
            var responseBody = result.getResponseBody();
            var expectedMessage = "movieInfo.name must be present,movieInfo.year must be a Positive value";
            assertEquals(expectedMessage, responseBody);
        });
    }

    @Test
    void updateMovieInfo() {

        var id = "abc";
        var movie = new MovieInfo("abc", "Batman Begins1", 2005,
                List.of("Cristian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(serviceMock.updateMovieInfo(movie, id)).thenReturn(Mono.just(movie));

        webClient
                .put()
                .uri("/v1/movies-info/abc")
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertTrue(new ReflectionEquals(savedMovieInfo).matches(movie));
                });
    }

    @Test
    void deleteMovieInfo() {

        when(serviceMock.deleteMovieInfo("abc")).thenReturn(Mono.empty());

        webClient
                .delete()
                .uri("/v1/movies-info/abc")
                .exchange()
                .expectStatus()
                .isNoContent();
    }


}
