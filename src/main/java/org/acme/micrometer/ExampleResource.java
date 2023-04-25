package org.acme.micrometer;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.LinkedList;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Path("/example")
@Produces("text/plain")
public class ExampleResource {
    private final LinkedList<Long> list = new LinkedList<>();

    private final MeterRegistry registry;

    ExampleResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Path("prime/{number}")
    @Timed
    public String checkIfPrime(long number) {
        if (number < 1) {
            registry.counter("example.prime.number", "type", "not-natural")
                    .increment();
            return "Only natural numbers can be prime numbers.";
        }
        if (number == 1) {
            registry.counter("example.prime.number", "type", "one")
                    .increment();
            return number + " is not prime.";
        }
        if (number == 2 || number % 2 == 0) {
            registry.counter("example.prime.number", "type", "even")
                    .increment();
            return number + " is not prime.";
        }

        if (timedTestPrimeNumber(number)) {
            registry.counter("example.prime.number", "type", "prime")
                    .increment();
            return number + " is prime.";
        } else {
            registry.counter("example.prime.number", "type", "not-prime")
                    .increment();
            return number + " is not prime.";
        }
    }

    protected boolean timedTestPrimeNumber(long number) {
        Timer timer = Timer.builder("example.prime.number.test")
                .description("Are number primes")
                .publishPercentiles(0.5, 0.95)
                .publishPercentileHistogram()
                .register(registry);
        return timer.record(() -> {
            boolean result = testPrimeNumber(number);
            return result;
        });
    }

    protected boolean testPrimeNumber(long number) {
        try {
            Thread.sleep(number);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (int i = 3; i < Math.floor(Math.sqrt(number)) + 1; i = i + 2) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }
}
