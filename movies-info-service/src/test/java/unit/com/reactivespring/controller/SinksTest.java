package com.reactivespring.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    @DisplayName("should demonstrate how sinks (replay all) work in reactive programming")
    public void sink() {
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        var integerFlux = replaySink.asFlux();

        integerFlux.subscribe((i) -> System.out.println("Subscriber 1: " + i));
        integerFlux.subscribe((i) -> System.out.println("Subscriber 2: " + i));

        replaySink.tryEmitNext(3);
    }

    @Test
    @DisplayName("should demonstrate how sinks (multicast) work in reactive programming")
    public void sinkMulticast() {
        Sinks.Many<Integer> multicastSink = Sinks.many().multicast().onBackpressureBuffer();

        multicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        var integerFlux1 = multicastSink.asFlux();

        integerFlux1.subscribe((i) -> System.out.println("Subscriber 1: " + i));

        var integerFlux2 = multicastSink.asFlux();
        integerFlux2.subscribe((i) -> System.out.println("Subscriber 2: " + i));

        multicastSink.tryEmitNext(3);
    }

    @Test
    @DisplayName("should demonstrate how sinks (unicast) work in reactive programming")
    public void sinkUnicast() {
        Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer(); // accepts only one subscriber

        unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        var integerFlux1 = unicastSink.asFlux();

        integerFlux1.subscribe((i) -> System.out.println("Subscriber 1: " + i));

        var integerFlux2 = unicastSink.asFlux();
        integerFlux2.subscribe((i) -> System.out.println("Subscriber 2: " + i));

        unicastSink.tryEmitNext(3);
    }
}
