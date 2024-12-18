package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {

    Flux<MovieInfo> findByYear(@NotNull @Positive(message = "movieInfo.year must be a Positive value") Integer year);

    Flux<MovieInfo> findByName(@NotNull String name);
}
