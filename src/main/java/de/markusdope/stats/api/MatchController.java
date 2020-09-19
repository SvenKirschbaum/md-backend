package de.markusdope.stats.api;

import de.markusdope.stats.data.dto.MatchDTO;
import de.markusdope.stats.data.repository.MatchPlayerRepository;
import de.markusdope.stats.data.repository.MatchRepository;
import de.markusdope.stats.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/match")
public class MatchController {
    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchPlayerRepository matchPlayerRepository;

    @GetMapping("/{id}")
    public Mono<MatchDTO> getMatchAction(@PathVariable Long id) {
        return matchPlayerRepository
                .findById(id)
                .flatMap(matchPlayer -> matchRepository.findById(id).map(match -> new MatchDTO(match, matchPlayer.getPlayers())))
                .switchIfEmpty(Mono.error(new NotFoundException()));
    }


    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/{id}")
    public Mono<Void> deleteMatchAction(@PathVariable Long id) {
        return matchPlayerRepository
                .deleteById(id)
                .and(matchRepository.deleteById(id));
    }
}
