package de.markusdope.stats.service;

import de.markusdope.stats.exception.BadGatewayException;
import de.markusdope.stats.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DataDragonServiceTest {
    @Test
    void resolvesMatchVersionAndCachesChampionMetadata() {
        AtomicInteger championRequests = new AtomicInteger();
        var webClient = WebClient.builder().baseUrl("https://example.test").exchangeFunction(request -> {
            String path = request.url().getPath();
            if (path.equals("/api/versions.json")) {
                return json("[\"15.14.1\",\"10.1.1\"]");
            }
            if (path.equals("/cdn/10.1.1/data/de_DE/champion.json")) {
                championRequests.incrementAndGet();
                return json("{\"data\":{\"Ashe\":{\"key\":\"22\",\"name\":\"Ashe\",\"image\":{\"full\":\"Ashe.png\"}}}}");
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        }).build();
        var service = new DataDragonService(webClient);

        StepVerifier.create(service.championName(22, "10.1.99.1234").repeat(1))
                .expectNext("Ashe", "Ashe")
                .verifyComplete();
        assertThat(championRequests).hasValue(1);
    }

    @Test
    void mapsMissingChampionToNotFound() {
        var service = serviceReturning("/api/versions.json", "[\"10.1.1\"]",
                "/cdn/10.1.1/data/de_DE/champion.json", "{\"data\":{}}");

        StepVerifier.create(service.championName(999, "10.1"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void returnsChampionImageBytes() {
        byte[] png = {1, 2, 3};
        var service = new DataDragonService(WebClient.builder().baseUrl("https://example.test")
                .exchangeFunction(request -> switch (request.url().getPath()) {
                    case "/api/versions.json" -> json("[\"10.1.1\"]");
                    case "/cdn/10.1.1/data/de_DE/champion.json" ->
                            json("{\"data\":{\"Ashe\":{\"key\":\"22\",\"name\":\"Ashe\",\"image\":{\"full\":\"Ashe.png\"}}}}");
                    case "/cdn/10.1.1/img/champion/Ashe.png" -> bytes(png);
                    default -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
                }).build());

        StepVerifier.create(service.championImage(22, "10.1"))
                .assertNext(actual -> assertThat(actual).containsExactly(png))
                .verifyComplete();
    }

    @Test
    void returnsSummonerSpellImageBytes() {
        byte[] png = {4, 5, 6};
        var service = new DataDragonService(WebClient.builder().baseUrl("https://example.test")
                .exchangeFunction(request -> switch (request.url().getPath()) {
                    case "/api/versions.json" -> json("[\"10.1.1\"]");
                    case "/cdn/10.1.1/data/de_DE/summoner.json" ->
                            json("{\"data\":{\"Flash\":{\"key\":\"4\",\"name\":\"Flash\",\"image\":{\"full\":\"SummonerFlash.png\"}}}}");
                    case "/cdn/10.1.1/img/spell/SummonerFlash.png" -> bytes(png);
                    default -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
                }).build());

        StepVerifier.create(service.summonerSpellImage(4, "10.1"))
                .assertNext(actual -> assertThat(actual).containsExactly(png))
                .verifyComplete();
    }

    @Test
    void returnsItemImageBytes() {
        byte[] png = {1, 2, 3};
        var webClient = WebClient.builder().baseUrl("https://example.test").exchangeFunction(request -> {
            if (request.url().getPath().equals("/api/versions.json")) return json("[\"10.1.1\"]");
            return bytes(png);
        }).build();

        StepVerifier.create(new DataDragonService(webClient).itemImage(1001, "10.1"))
                .assertNext(actual -> assertThat(actual).containsExactly(png))
                .verifyComplete();
    }

    @Test
    void mapsImage404ToNotFound() {
        StepVerifier.create(serviceWithImageStatus(HttpStatus.NOT_FOUND).itemImage(1001, "10.1"))
                .expectError(NotFoundException.class).verify();
    }

    @Test
    void mapsImage500ToBadGateway() {
        StepVerifier.create(serviceWithImageStatus(HttpStatus.INTERNAL_SERVER_ERROR).itemImage(1001, "10.1"))
                .expectError(de.markusdope.stats.exception.BadGatewayException.class).verify();
    }

    @Test
    void mapsMalformedMetadataToBadGateway() {
        var service = serviceReturning("/api/versions.json", "[\"10.1.1\"]",
                "/cdn/10.1.1/data/de_DE/champion.json", "{not-json");

        StepVerifier.create(service.championName(22, "10.1"))
                .expectError(de.markusdope.stats.exception.BadGatewayException.class).verify();
    }

    @Test
    void mapsEmptyVersionResponseToBadGateway() {
        var service = new DataDragonService(WebClient.builder().baseUrl("https://example.test")
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK).build()))
                .build());

        StepVerifier.create(service.resolveVersion("latest"))
                .expectError(BadGatewayException.class)
                .verify();
    }

    @Test
    void mapsEmptyChampionCatalogResponseToBadGateway() {
        var service = new DataDragonService(WebClient.builder().baseUrl("https://example.test")
                .exchangeFunction(request -> request.url().getPath().equals("/api/versions.json")
                        ? json("[\"10.1.1\"]")
                        : Mono.just(ClientResponse.create(HttpStatus.OK).build()))
                .build());

        StepVerifier.create(service.championName(22, "10.1"))
                .expectError(BadGatewayException.class)
                .verify();
    }

    @Test
    void mapsEmptyImageResponseToBadGateway() {
        StepVerifier.create(serviceWithImageStatus(HttpStatus.OK).itemImage(1001, "10.1"))
                .expectError(BadGatewayException.class)
                .verify();
    }

    @Test
    void mapsRedirectToBadGateway() {
        StepVerifier.create(serviceWithImageStatus(HttpStatus.FOUND).itemImage(1001, "10.1"))
                .expectError(BadGatewayException.class)
                .verify();
    }

    private static Mono<ClientResponse> json(String body) {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
    }

    private static Mono<ClientResponse> bytes(byte[] body) {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .body(new String(body, StandardCharsets.ISO_8859_1))
                .build());
    }

    private static DataDragonService serviceReturning(String firstPath, String firstBody,
                                                       String secondPath, String secondBody) {
        return new DataDragonService(WebClient.builder().baseUrl("https://example.test").exchangeFunction(request -> {
            String path = request.url().getPath();
            if (path.equals(firstPath)) return json(firstBody);
            if (path.equals(secondPath)) return json(secondBody);
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        }).build());
    }

    private static DataDragonService serviceWithImageStatus(HttpStatus status) {
        return new DataDragonService(WebClient.builder().baseUrl("https://example.test").exchangeFunction(request -> {
            if (request.url().getPath().equals("/api/versions.json")) return json("[\"10.1.1\"]");
            return Mono.just(ClientResponse.create(status).build());
        }).build());
    }
}
