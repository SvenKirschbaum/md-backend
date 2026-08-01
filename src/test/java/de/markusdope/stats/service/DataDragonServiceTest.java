package de.markusdope.stats.service;

import de.markusdope.stats.exception.BadGatewayException;
import de.markusdope.stats.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DataDragonServiceTest {
    private static final byte[] PNG = {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };

    @Test
    void refreshesVersionListAfterConfiguredTtl() {
        assertThat(DataDragonService.VERSION_LIST_CACHE_TTL).isEqualTo(Duration.ofHours(1));
        AtomicInteger versionRequests = new AtomicInteger();
        var webClient = WebClient.builder().baseUrl("https://example.test").exchangeFunction(request ->
                json(versionRequests.incrementAndGet() == 1 ? "[\"10.1.1\"]" : "[\"10.2.1\"]"))
                .build();
        var service = new DataDragonService(webClient, Duration.ZERO);

        StepVerifier.create(service.resolveVersion("latest"))
                .expectNext("10.1.1")
                .verifyComplete();
        StepVerifier.create(service.resolveVersion("latest"))
                .expectNext("10.2.1")
                .verifyComplete();
        assertThat(versionRequests).hasValue(2);
    }

    @Test
    void retriesVersionRequestAfterFailure() {
        AtomicInteger versionRequests = new AtomicInteger();
        var webClient = WebClient.builder().baseUrl("https://example.test").exchangeFunction(request -> {
            if (versionRequests.incrementAndGet() == 1) {
                return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
            }
            return json("[\"10.1.1\"]");
        }).build();
        var service = new DataDragonService(webClient);

        StepVerifier.create(service.resolveVersion("latest"))
                .expectError(BadGatewayException.class)
                .verify();
        StepVerifier.create(service.resolveVersion("latest"))
                .expectNext("10.1.1")
                .verifyComplete();
        assertThat(versionRequests).hasValue(2);
    }

    @Test
    void sharesFailedChampionCatalogRequestThenRetries() {
        AtomicInteger championRequests = new AtomicInteger();
        Sinks.One<ClientResponse> firstResponse = Sinks.one();
        var webClient = WebClient.builder().baseUrl("https://example.test").exchangeFunction(request -> {
            String path = request.url().getPath();
            if (path.equals("/api/versions.json")) return json("[\"10.1.1\"]");
            if (championRequests.incrementAndGet() == 1) return firstResponse.asMono();
            return json("{\"data\":{\"Ashe\":{\"key\":\"22\",\"name\":\"Ashe\",\"image\":{\"full\":\"Ashe.png\"}}}}");
        }).build();
        var service = new DataDragonService(webClient);

        StepVerifier.create(Mono.whenDelayError(
                        service.championName(22, "10.1"), service.championName(22, "10.1")))
                .then(() -> firstResponse.tryEmitValue(
                        ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()))
                .expectError()
                .verify();
        assertThat(championRequests).hasValue(1);

        StepVerifier.create(service.championName(22, "10.1"))
                .expectNext("Ashe")
                .verifyComplete();
        assertThat(championRequests).hasValue(2);
    }

    @Test
    void retriesSpellCatalogRequestAfterFailure() {
        AtomicInteger spellRequests = new AtomicInteger();
        var webClient = WebClient.builder().baseUrl("https://example.test").exchangeFunction(request -> {
            String path = request.url().getPath();
            if (path.equals("/api/versions.json")) return json("[\"10.1.1\"]");
            if (!path.equals("/cdn/10.1.1/data/de_DE/summoner.json")) {
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            }
            if (spellRequests.incrementAndGet() == 1) {
                return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
            }
            return json("{\"data\":{\"Flash\":{\"key\":\"4\",\"name\":\"Flash\",\"image\":{\"full\":\"SummonerFlash.png\"}}}}");
        }).build();
        var service = new DataDragonService(webClient);

        StepVerifier.create(service.summonerSpellImage(4, "10.1"))
                .expectError(BadGatewayException.class)
                .verify();
        StepVerifier.create(service.summonerSpellImage(4, "10.1"))
                .expectError(NotFoundException.class)
                .verify();
        StepVerifier.create(service.summonerSpellImage(4, "10.1"))
                .expectError(NotFoundException.class)
                .verify();
        assertThat(spellRequests).hasValue(2);
    }

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
        var service = new DataDragonService(WebClient.builder().baseUrl("https://example.test")
                .exchangeFunction(request -> switch (request.url().getPath()) {
                    case "/api/versions.json" -> json("[\"10.1.1\"]");
                    case "/cdn/10.1.1/data/de_DE/champion.json" ->
                            json("{\"data\":{\"Ashe\":{\"key\":\"22\",\"name\":\"Ashe\",\"image\":{\"full\":\"Ashe.png\"}}}}");
                    case "/cdn/10.1.1/img/champion/Ashe.png" -> bytes(PNG);
                    default -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
                }).build());

        StepVerifier.create(service.championImage(22, "10.1"))
                .assertNext(actual -> assertThat(actual).containsExactly(PNG))
                .verifyComplete();
    }

    @Test
    void returnsSummonerSpellImageBytes() {
        var service = new DataDragonService(WebClient.builder().baseUrl("https://example.test")
                .exchangeFunction(request -> switch (request.url().getPath()) {
                    case "/api/versions.json" -> json("[\"10.1.1\"]");
                    case "/cdn/10.1.1/data/de_DE/summoner.json" ->
                            json("{\"data\":{\"Flash\":{\"key\":\"4\",\"name\":\"Flash\",\"image\":{\"full\":\"SummonerFlash.png\"}}}}");
                    case "/cdn/10.1.1/img/spell/SummonerFlash.png" -> bytes(PNG);
                    default -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
                }).build());

        StepVerifier.create(service.summonerSpellImage(4, "10.1"))
                .assertNext(actual -> assertThat(actual).containsExactly(PNG))
                .verifyComplete();
    }

    @Test
    void returnsItemImageBytes() {
        var webClient = WebClient.builder().baseUrl("https://example.test").exchangeFunction(request -> {
            if (request.url().getPath().equals("/api/versions.json")) return json("[\"10.1.1\"]");
            return bytes(PNG);
        }).build();

        StepVerifier.create(new DataDragonService(webClient).itemImage(1001, "10.1"))
                .assertNext(actual -> assertThat(actual).containsExactly(PNG))
                .verifyComplete();
    }

    @Test
    void mapsHtmlImageResponseToBadGateway() {
        var service = new DataDragonService(WebClient.builder().baseUrl("https://example.test")
                .exchangeFunction(request -> request.url().getPath().equals("/api/versions.json")
                        ? json("[\"10.1.1\"]")
                        : Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", MediaType.TEXT_HTML_VALUE)
                        .body("<html>not png</html>")
                        .build()))
                .build());

        StepVerifier.create(service.itemImage(1001, "10.1"))
                .expectError(BadGatewayException.class)
                .verify();
    }

    @Test
    void mapsWrongPngSignatureToBadGateway() {
        byte[] wrongSignature = {0, 1, 2, 3, 4, 5, 6, 7};
        var service = new DataDragonService(WebClient.builder().baseUrl("https://example.test")
                .exchangeFunction(request -> request.url().getPath().equals("/api/versions.json")
                        ? json("[\"10.1.1\"]") : bytes(wrongSignature))
                .build());

        StepVerifier.create(service.itemImage(1001, "10.1"))
                .expectError(BadGatewayException.class)
                .verify();
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
                .header("Content-Type", "image/png;source=test")
                .body(Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(body)))
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
