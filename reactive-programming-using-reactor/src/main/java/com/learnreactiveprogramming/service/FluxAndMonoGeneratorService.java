package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Mono<String> namesMono() {
        return Mono.just("alex").log();
    }

    public Mono<String> namesMonoMapFilter(int stringLength) {
        return Mono.just("alex").map(String::toUpperCase).filter(s -> s.length() > stringLength).log();
    }

    public Mono<List<String>> namesMonoFlatMap(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono)
                .log();
    }

    public Flux<String> namesMonoFlatMapMany(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString)
                .log();
    }

    private Mono<List<String>> splitStringMono(String s) {
        var charArray = s.split("");
        return Mono.just(List.of(charArray));
    }


    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe")).log();
    }

    public Flux<String> namesFluxUpperCaseAndFilter(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter((name) -> name.length() > stringLength)
                .map((name) -> String.format("%s-%s", name.length(), name))
                .log();
    }

    public Flux<String> namesFluxMapImmutability() {
        var nameList = Flux.fromIterable(List.of("alex", "ben", "chloe"));

        nameList.map(String::toUpperCase);

        return nameList;
    }

    public Flux<String> namesFluxFlatMap(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter((name) -> name.length() > stringLength)
                .flatMap(this::splitString)
                .log();
    }

    public Flux<String> namesFluxTransform(int stringLength) {

        Function<Flux<String>, Flux<String>> filtermap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filtermap)
                .flatMap(this::splitString)
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFluxTransform_switchIfEmpty(int stringLength) {

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitString);

        var defaultFlux = Flux.just("default").transform(filterMap);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterMap)
                .flatMap(this::splitString)
                .switchIfEmpty(defaultFlux)
                .log();
    }

    public Flux<String> splitString(String name) {
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    public Flux<String> namesFluxFlatMapAsync(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter((name) -> name.length() > stringLength)
                .flatMap(this::splitStringDelay)
                .log();
    }

    public Flux<String> namesFluxConcat(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter((name) -> name.length() > stringLength)
                .concatMap(this::splitStringDelay)
                .log();
    }

    public Flux<String> exploreConcat() {
       var abcFlux = Flux.just("A", "B", "C");
       var defFlux = Flux.just("D", "E", "F");

       return Flux.concat(abcFlux, defFlux).log();
    }

    // same behaviour as above, you can concat two flux or two monos in a flux
    public Flux<String> exploreConcatWith() {
        var abcFlux = Mono.just("A");
        var defFlux = Mono.just("B");

        return abcFlux.concatWith(defFlux).log();
    }

    public Flux<String> exploreMerge() {
        var abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));

        return Flux.merge(abcFlux, defFlux).log(); // mergeWith is the same
    }

    public Flux<String> exploreZip() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");

        // you can also map over the tuple
        // Flux.zip(abcFlux, defFlux).map(t4 -> t4.getT1() + t4.getT2());
        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second).log(); // mergeWith is the same
    }

    public Flux<String> splitStringDelay(String name) {
        var charArray = name.split("");
        int delay = new Random().nextInt(1000);
        return Flux.fromArray(charArray).delayElements(Duration.ofMillis(delay));
    }

    public static void main(String[] args) {

        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux().subscribe((name) -> System.out.println("Name is: " + name));

        fluxAndMonoGeneratorService.namesMono().subscribe((name) -> System.out.println("Mono name is: " + name));
    }
}
