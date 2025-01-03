package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesMonoFlatMap () {
        var mono = fluxAndMonoGeneratorService.namesMonoFlatMap(3);
        StepVerifier.create(mono).expectNext(List.of("A", "L", "E", "X")).verifyComplete();
    }

    @Test
    void namesMonoFlatMapMany() {
        var mono = fluxAndMonoGeneratorService.namesMonoFlatMapMany(3);
        StepVerifier.create(mono).expectNext("A", "L", "E", "X").verifyComplete();
    }

    @Test
    void namesFlux() throws Exception {
        var namesFlux  = fluxAndMonoGeneratorService.namesFlux();

        StepVerifier.create(namesFlux).expectNext("alex", "ben", "chloe").expectNextCount(0).verifyComplete();
    }

    @Test
    void namesFluxUpperCaseAndFilter() throws Exception {
        var namesFluxMap  = fluxAndMonoGeneratorService.namesFluxUpperCaseAndFilter(3);

        StepVerifier.create(namesFluxMap).expectNext("4-ALEX", "5-CHLOE").expectNextCount(0).verifyComplete();
    }


    @Test
    void namesFluxMapImmutability() throws Exception {
        var namesFluxMap  = fluxAndMonoGeneratorService.namesFluxMapImmutability();

        StepVerifier.create(namesFluxMap).expectNext("alex", "ben", "chloe").expectNextCount(0).verifyComplete();
    }

    @Test
    void namesFluxFlatMap() throws Exception {
        var flux = fluxAndMonoGeneratorService.namesFluxFlatMap(3);

        StepVerifier.create(flux).expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E").expectNextCount(0).verifyComplete();
    }

    @Test
    void namesFluxTransform() throws Exception {
        var flux = fluxAndMonoGeneratorService.namesFluxTransform(3);

        StepVerifier.create(flux).expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E").expectNextCount(0).verifyComplete();
    }

    @Test
    void namesFluxTransform_1() throws Exception {
        var flux = fluxAndMonoGeneratorService.namesFluxTransform(6);

        StepVerifier.create(flux).expectNext("default").expectNextCount(0).verifyComplete();
    }

    @Test
    void namesFluxTransform_switchIfEmpty() throws Exception {
        var flux = fluxAndMonoGeneratorService.namesFluxTransform_switchIfEmpty(6);

        StepVerifier.create(flux).expectNext("D", "E", "F", "A", "U", "L", "T").expectNextCount(0).verifyComplete();
    }

    @Test
    void namesFluxFlatMapAsync () {
        var flux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync(3);

        StepVerifier.create(flux).expectNextCount(9).verifyComplete();
    }

    @Test
    void namesFluxConcatMap() throws Exception {
        var flux = fluxAndMonoGeneratorService.namesFluxConcat(3);

        StepVerifier.create(flux).expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E").expectNextCount(0).verifyComplete();
    }

    @Test
    void exploreConcat() {
        var flux = fluxAndMonoGeneratorService.exploreConcat();

        StepVerifier.create(flux).expectNext("A", "B", "C", "D", "E", "F").expectNextCount(0).verifyComplete();

    }

    @Test
    void exploreMerge() {
        var flux = fluxAndMonoGeneratorService.exploreMerge();

        StepVerifier.create(flux).expectNext("A", "D", "B", "E", "C", "F").expectNextCount(0).verifyComplete();

    }

    @Test
    void exploreZip() {
        var flux = fluxAndMonoGeneratorService.exploreZip();

        StepVerifier.create(flux).expectNext("AD", "BE", "CF").expectNextCount(0).verifyComplete();

    }

}