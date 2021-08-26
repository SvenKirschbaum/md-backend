package de.markusdope.stats.api;

import de.markusdope.stats.config.MarkusDopeStatsProperties;
import de.markusdope.stats.data.dto.LolRecordsDTO;
import de.markusdope.stats.data.dto.PlayerStats;
import de.markusdope.stats.data.dto.SeasonDTO;
import de.markusdope.stats.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @Autowired
    private MarkusDopeStatsProperties properties;

    @GetMapping(value = {"/player", "/player/{season}"})
    public Flux<PlayerStats> getPlayerStats(@PathVariable Optional<Integer> season) {
        return statsService.getPlayerStats(season.orElse(properties.getCurrentSeason()));
    }

    @GetMapping(value = {"/records", "/records/{season}"})
    public Mono<LolRecordsDTO> getRecords(@PathVariable Optional<Integer> season) {
        return statsService.getRecords(season.orElse(properties.getCurrentSeason()));
    }

    @GetMapping("/season")
    public Mono<SeasonDTO> getCurrentSeason() {
        return Mono.just(new SeasonDTO(properties.getCurrentSeason()));
    }
}
