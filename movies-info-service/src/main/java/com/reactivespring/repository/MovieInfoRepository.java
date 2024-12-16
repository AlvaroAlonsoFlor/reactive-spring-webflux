package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {

    Flux<MovieInfo> findByYear(@NotNull @Positive(message = "movieInfo.year must be a Positive value") Integer year);

    Flux<MovieInfo> findByName(@NotNull String name);
}
