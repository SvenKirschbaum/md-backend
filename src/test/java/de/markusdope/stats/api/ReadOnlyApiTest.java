package de.markusdope.stats.api;

import de.markusdope.stats.config.MarkusDopeStatsProperties;
import de.markusdope.stats.data.repository.MatchPlayerRepository;
import de.markusdope.stats.data.repository.MatchRepository;
import de.markusdope.stats.exception.ReadOnlyExceptionHandler;
import de.markusdope.stats.service.DataDragonService;
import de.markusdope.stats.service.PlayerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReadOnlyApiTest {
    private static final String READ_ONLY_REASON = "Match data is read-only";

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchPlayerRepository matchPlayerRepository;
    @Mock
    private PlayerService playerService;
    @Mock
    private MarkusDopeStatsProperties properties;
    @Mock
    private DataDragonService dataDragonService;

    private WebTestClient webTestClient;
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        ImportController importController = new ImportController();
        MatchController matchController = new MatchController();

        context = new AnnotationConfigApplicationContext();
        context.register(WebFluxTestConfiguration.class);
        context.getBeanFactory().registerSingleton("matchRepository", matchRepository);
        context.getBeanFactory().registerSingleton("matchPlayerRepository", matchPlayerRepository);
        context.getBeanFactory().registerSingleton("playerService", playerService);
        context.getBeanFactory().registerSingleton("properties", properties);
        context.getBeanFactory().registerSingleton("dataDragonService", dataDragonService);
        context.registerBean(ImportController.class, () -> importController);
        context.registerBean(MatchController.class, () -> matchController);
        context.registerBean(ReadOnlyExceptionHandler.class);
        context.refresh();

        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void rejectsImportPreviewWithoutCallingDataSources() {
        assertReadOnly(webTestClient.get().uri("/import/42").exchange());

        verifyDataSourcesWereNotCalled();
    }

    @Test
    void rejectsImportWithNoBodyWithoutCallingDataSources() {
        assertReadOnly(webTestClient.post().uri("/import/42").exchange());

        verifyDataSourcesWereNotCalled();
    }

    @Test
    void rejectsImportWithEmptyJsonBodyWithoutCallingDataSources() {
        assertReadOnly(webTestClient.post().uri("/import/42")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("")
                .exchange());

        verifyDataSourcesWereNotCalled();
    }

    @Test
    void rejectsImportWithEmptyJsonObjectWithoutCallingDataSources() {
        assertReadOnly(webTestClient.post().uri("/import/42")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange());

        verifyDataSourcesWereNotCalled();
    }

    @Test
    void rejectsImportWithMalformedJsonWithoutCallingDataSources() {
        assertReadOnly(webTestClient.post().uri("/import/42")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{")
                .exchange());

        verifyDataSourcesWereNotCalled();
    }

    @Test
    void rejectsImportWithUnsupportedContentTypeWithoutCallingDataSources() {
        assertReadOnly(webTestClient.post().uri("/import/42")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("not json")
                .exchange());

        verifyDataSourcesWereNotCalled();
    }

    @Test
    void rejectsImportWithValidJsonWithoutCallingDataSources() {
        assertReadOnly(webTestClient.post().uri("/import/42")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"playerMapping\":{\"1\":\"Alice\"}}")
                .exchange());

        verifyDataSourcesWereNotCalled();
    }

    @Test
    void rejectsMatchDeletionWithoutCallingDataSources() {
        assertReadOnly(webTestClient.delete().uri("/match/42").exchange());

        verifyDataSourcesWereNotCalled();
    }

    private void assertReadOnly(WebTestClient.ResponseSpec response) {
        response.expectStatus().isEqualTo(HttpStatus.GONE)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(410)
                .jsonPath("$.detail").isEqualTo(READ_ONLY_REASON);
    }

    private void verifyDataSourcesWereNotCalled() {
        verifyNoInteractions(matchRepository, matchPlayerRepository, playerService, properties, dataDragonService);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebFlux
    static class WebFluxTestConfiguration {
    }
}
