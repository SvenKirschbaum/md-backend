package de.markusdope.stats.service;

import de.markusdope.stats.exception.BadGatewayException;
import de.markusdope.stats.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DataDragonService {
    private static final String BASE_URL = "https://ddragon.leagueoflegends.com";

    private final WebClient webClient;
    private final Mono<List<String>> versions;
    private final Map<String, Mono<Catalog>> champions = new ConcurrentHashMap<>();
    private final Map<String, Mono<Catalog>> spells = new ConcurrentHashMap<>();

    @Autowired
    public DataDragonService(WebClient.Builder builder) {
        this(builder.baseUrl(BASE_URL).build());
    }

    DataDragonService(WebClient webClient) {
        this.webClient = webClient;
        this.versions = get("/api/versions.json", new ParameterizedTypeReference<List<String>>() {
        }).cache();
    }

    public Mono<String> resolveVersion(String requested) {
        return mapFailures(versions.flatMap(available -> {
            if ("latest".equals(requested)) return Mono.just(available.getFirst());
            if (available.contains(requested)) return Mono.just(requested);
            String[] parts = requested.split("\\.");
            if (parts.length < 2) return Mono.error(new NotFoundException());
            String prefix = parts[0] + "." + parts[1] + ".";
            return available.stream()
                    .filter(version -> version.startsWith(prefix))
                    .findFirst()
                    .map(Mono::just)
                    .orElseGet(() -> Mono.error(new NotFoundException()));
        }));
    }

    public Mono<String> championName(int championId, String requestedVersion) {
        return mapFailures(championNames(List.of(championId), requestedVersion)
                .map(names -> names.get(championId))
                .switchIfEmpty(Mono.error(new NotFoundException())));
    }

    public Mono<Map<Integer, String>> championNames(Collection<Integer> championIds, String requestedVersion) {
        return mapFailures(resolveVersion(requestedVersion).flatMap(version -> championCatalog(version).map(catalog -> {
            Map<Integer, String> names = catalog.data().values().stream()
                    .filter(entry -> championIds.contains(Integer.parseInt(entry.key())))
                    .collect(Collectors.toMap(entry -> Integer.parseInt(entry.key()), Entry::name));
            if (names.size() != championIds.stream().distinct().count()) throw new NotFoundException();
            return names;
        })));
    }

    public Mono<byte[]> championImage(int championId, String requestedVersion) {
        return mapFailures(resolveVersion(requestedVersion).flatMap(version -> championCatalog(version)
                .map(catalog -> find(catalog, championId).image().full())
                .flatMap(file -> getBytes("/cdn/" + version + "/img/champion/" + file))));
    }

    public Mono<byte[]> summonerSpellImage(int spellId, String requestedVersion) {
        return mapFailures(resolveVersion(requestedVersion).flatMap(version -> spellCatalog(version)
                .map(catalog -> find(catalog, spellId).image().full())
                .flatMap(file -> getBytes("/cdn/" + version + "/img/spell/" + file))));
    }

    public Mono<byte[]> itemImage(int itemId, String requestedVersion) {
        return mapFailures(resolveVersion(requestedVersion)
                .flatMap(version -> getBytes("/cdn/" + version + "/img/item/" + itemId + ".png")));
    }

    private Mono<Catalog> championCatalog(String version) {
        return champions.computeIfAbsent(version,
                key -> get("/cdn/" + key + "/data/de_DE/champion.json", Catalog.class).cache());
    }

    private Mono<Catalog> spellCatalog(String version) {
        return spells.computeIfAbsent(version,
                key -> get("/cdn/" + key + "/data/de_DE/summoner.json", Catalog.class).cache());
    }

    private Entry find(Catalog catalog, int id) {
        return catalog.data().values().stream()
                .filter(entry -> Integer.parseInt(entry.key()) == id)
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    private <T> Mono<T> get(String path, Class<T> type) {
        return exchange(path, response -> response.bodyToMono(type));
    }

    private <T> Mono<T> get(String path, ParameterizedTypeReference<T> type) {
        return exchange(path, response -> response.bodyToMono(type));
    }

    private Mono<byte[]> getBytes(String path) {
        return exchange(path, response -> response.bodyToMono(byte[].class));
    }

    private <T> Mono<T> exchange(String path, Function<ClientResponse, Mono<T>> body) {
        return webClient.get().uri(path).exchangeToMono(response -> {
            HttpStatusCode status = response.statusCode();
            if (status.value() == 404) return Mono.error(new NotFoundException());
            if (!status.is2xxSuccessful()) return Mono.error(new BadGatewayException());
            return body.apply(response).switchIfEmpty(Mono.error(new BadGatewayException()));
        }).timeout(Duration.ofSeconds(10)).transform(this::mapFailures);
    }

    private <T> Mono<T> mapFailures(Mono<T> result) {
        return result.onErrorMap(error -> error instanceof NotFoundException || error instanceof BadGatewayException
                ? error : new BadGatewayException(error));
    }

    private record Catalog(Map<String, Entry> data) {
    }

    private record Entry(String key, String name, Image image) {
    }

    private record Image(String full) {
    }
}
