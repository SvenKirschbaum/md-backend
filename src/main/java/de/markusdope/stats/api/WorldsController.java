package de.markusdope.stats.api;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

@RestController
public class WorldsController {

    private WebClient webClient = WebClient.create();
    private final Mono<String> schedule =
            Mono.defer(
                    () ->
                            webClient
                                    .get()
                                    .uri("https://lol.gamepedia.com/api.php?action=cargoquery&format=json&smaxage=0&tables=MatchSchedule&fields=DateTime_UTC,Team1,Team2&where=OverviewPage LIKE \"2021 Season World Championship/%\"&limit=max")
                                    .retrieve()
                                    .bodyToMono(WorldsScheduleResponse.class)
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
                    .cache(Duration.ofHours(1));


    @GetMapping(value = "/worlds/schedule.icu", produces = "text/calendar")
    public Mono<String> getWorldsSchedule() {
        return this.schedule;

    }
}