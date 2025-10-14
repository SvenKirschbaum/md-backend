package de.markusdope.stats.api;

import de.markusdope.stats.config.MarkusDopeStatsProperties;
import de.markusdope.stats.data.dto.LoginToken;
import de.markusdope.stats.data.dto.WorldsScheduleResponse;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;

@RestController
public class WorldsController {

    @Autowired
    private MarkusDopeStatsProperties properties;

    private static final String BASE_API_URL = "https://lol.fandom.com/api.php";
    private static final String TOKEN_SESSION_COOKIE_NAME = "fandom__session";
    private static final String AUTH_SESSION_COOKIE_NAME = "fandom__BPsession";

    private final WebClient webClient = WebClient.create();

    private final Mono<LoginToken> loginToken =
            Mono.defer(
                    () ->
                            webClient
                                    .get()
                                    .uri(BASE_API_URL + "?action=query&format=json&meta=tokens&type=login")
                                    .exchangeToMono(response -> {
                                        ResponseCookie tokenCookie = response.cookies().get(TOKEN_SESSION_COOKIE_NAME).getFirst();

                                        return response.bodyToMono(LoginToken.LoginTokenResponse.class).flatMap(
                                                loginTokenResponse -> Mono.just(new LoginToken(loginTokenResponse.getQuery().getTokens().getLogintoken(), tokenCookie.getValue()))
                                        );
                                    })
            );

    private final Mono<String> loginCookie =
            Mono.defer(
                    () ->
                            loginToken.flatMap(
                                    loginToken ->
                                            webClient
                                                .post()
                                                .uri(BASE_API_URL)
                                                .body(
                                                    BodyInserters
                                                        .fromFormData("action", "login")
                                                        .with("format", "json")
                                                        .with("lgname", properties.getLolFandomUsername())
                                                        .with("lgpassword", properties.getLolFandomPassword())
                                                        .with("lgtoken", loginToken.getToken())
                                                )
                                                .cookie(TOKEN_SESSION_COOKIE_NAME, loginToken.getCookie())
                                                .exchangeToMono(response -> Mono.just(response.cookies().get(AUTH_SESSION_COOKIE_NAME).getFirst().getValue()))
                            )
            );

    private final Mono<String> schedule =
            Mono.defer(
                    () ->
                            loginCookie
                                    .flatMap(loginCookie ->
                                        webClient
                                            .get()
                                            .uri(BASE_API_URL + "?action=cargoquery&format=json&smaxage=0&tables=MatchSchedule&fields=DateTime_UTC,Team1,Team2&where=OverviewPage LIKE \"2025 Season World Championship/%\"&limit=max")
                                            .cookie(AUTH_SESSION_COOKIE_NAME, loginCookie)
                                            .retrieve()
                                            .bodyToMono(WorldsScheduleResponse.class)
                                    )

            )
                    .flatMapIterable(worldsScheduleResponse -> Arrays.asList(worldsScheduleResponse.getCargoquery()))
                    .map(WorldsScheduleResponse.ElementWrapper::getTitle)
                    .collectList()
                    .map(worldsScheduleMatches -> worldsScheduleMatches.toArray(WorldsScheduleResponse.WorldsScheduleMatch[]::new))
                    .map(worldsScheduleMatches -> {
                        Calendar calendar = new Calendar();
                        calendar.getProperties().add(new ProdId("//www.markus-dope.de/api/worlds/schedule.icu"));
                        calendar.getProperties().add(Version.VERSION_2_0);
                        calendar.getProperties().add(CalScale.GREGORIAN);

                        ComponentList components = calendar.getComponents();

                        for (WorldsScheduleResponse.WorldsScheduleMatch match : worldsScheduleMatches) {
                            if(match.getInstant() == null) {
                                continue;
                            }

                            DateTime startDate = new DateTime(Date.from(match.getInstant()));
                            startDate.setUtc(true);
                            DateTime endDate = new DateTime(Date.from(match.getInstant().plus(Duration.ofHours(1))));
                            endDate.setUtc(true);
                            VEvent event = new VEvent(startDate, endDate, String.format("%s vs %s", match.getTeam1(), match.getTeam2()));
                            Uid uid = new Uid(String.valueOf(match.getInstant().toString().hashCode()));
                            event.getProperties().add(uid);
                            components.add(event);
                        }

                        return calendar.toString();
                    })
                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                    .cache((v) -> Duration.ofHours(1), (v) -> Duration.ZERO, () -> Duration.ZERO);


    @GetMapping(value = "/worlds/schedule.icu", produces = "text/calendar")
    public Mono<String> getWorldsSchedule() {
        return this.schedule;

    }
}
