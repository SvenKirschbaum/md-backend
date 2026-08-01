package de.markusdope.stats.api;

import de.markusdope.stats.service.DataDragonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

class StaticLoLDataControllerTest {
    @Mock
    private DataDragonService dataDragonService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        StaticLoLDataController controller = new StaticLoLDataController(dataDragonService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void returnsChampionPngFromDataDragon() {
        byte[] png = {1, 2, 3};
        when(dataDragonService.championImage(22, "10.1")).thenReturn(Mono.just(png));

        webTestClient.get().uri("/static/10.1/champion/image/22").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_PNG)
                .expectBody(byte[].class).isEqualTo(png);
    }
}
