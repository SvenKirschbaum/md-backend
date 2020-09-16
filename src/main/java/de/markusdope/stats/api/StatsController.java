package de.markusdope.stats.api;

import de.markusdope.stats.data.dto.LolRecords;
import de.markusdope.stats.data.dto.PlayerStats;
import de.markusdope.stats.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/player")
    public Flux<PlayerStats> getPlayerStats() {
        return statsService.getPlayerStats();
    }

    @GetMapping("/records")
    public Mono<LolRecords> getRecords() {
        return statsService.getRecords();
    }
}
