package de.markusdope.stats.api;

import de.markusdope.stats.config.MarkusDopeStatsProperties;
import de.markusdope.stats.data.document.MatchPlayer;
import de.markusdope.stats.data.dto.MatchDTO;
import de.markusdope.stats.data.dto.PlayerMatchDTO;
import de.markusdope.stats.data.match.Match;
import de.markusdope.stats.data.match.Participant;
import de.markusdope.stats.data.repository.MatchPlayerRepository;
import de.markusdope.stats.data.repository.MatchRepository;
import de.markusdope.stats.exception.NotFoundException;
import de.markusdope.stats.service.DataDragonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/match")
public class MatchController {
    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchPlayerRepository matchPlayerRepository;

    @Autowired
    private MarkusDopeStatsProperties properties;

    @Autowired
    private DataDragonService dataDragonService;

    @GetMapping("/{id}")
    public Mono<MatchDTO> getMatchAction(@PathVariable Long id) {
        return matchPlayerRepository
                .findById(id)
                .map(MatchPlayer::getPlayers)
                .map(players -> {
                    Map<Integer, String> playerMap = new HashMap<>();
                    for (MatchPlayer.Player p : players) {
                        playerMap.put(p.getParticipantId(), p.getName());
                    }
                    return playerMap;
                })
                .flatMap(playerMap -> matchRepository.findById(id).map(match -> new MatchDTO(match.getMatch(), playerMap, match.getSeason())))
                .switchIfEmpty(Mono.error(new NotFoundException()));
    }


    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/{id}")
    public Mono<Void> deleteMatchAction(@PathVariable Long id) {
        return matchPlayerRepository
                .deleteById(id)
                .and(matchRepository.deleteById(id));
    }

    @GetMapping(value = {"/player/{name}/", "/player/{name}/{season}"})
    public Flux<PlayerMatchDTO> getPlayerMatches(@PathVariable String name, @PathVariable Optional<Integer> season) {
        return matchPlayerRepository
                .findAllByPlayer(name)
                .flatMap(matchPlayer ->
                        matchRepository
                                .findById(matchPlayer.getId())
                                .filter(matchDocument -> {
                                    Integer searchedSeason = season.orElse(properties.getCurrentSeason());

                                    //Handle season = 0 as all season combined
                                    return searchedSeason == 0 || matchDocument.getSeason().equals(searchedSeason);
                                })
                                .flatMap(matchDocument -> {
                                    Match match = matchDocument.getMatch();
                                    Integer participantId = matchPlayer.getParticipant(name);

                                    PlayerMatchDTO playerMatchDTO = new PlayerMatchDTO();
                                    playerMatchDTO.setMatchId(match.getId());

                                    Participant p = match.getParticipants().stream()
                                            .filter(candidate -> candidate.getParticipantId() == participantId)
                                            .findFirst()
                                            .orElse(null);

                                    if (p.getTeam() == match.getBlueTeam().getTeamId()) {
                                        playerMatchDTO.setWin(match.getBlueTeam().isWinner());
                                    } else {
                                        playerMatchDTO.setWin(match.getRedTeam().isWinner());
                                    }

                                    playerMatchDTO.setPlayer(p);

                                    playerMatchDTO.setMatchCreationTime(match.getCreationTime().toInstant());
                                    playerMatchDTO.setMatchDuration(match.getDuration());
                                    playerMatchDTO.setVersion(match.getVersion());

                                    return dataDragonService.championName(p.getChampionId(), match.getVersion())
                                            .map(champion -> {
                                                playerMatchDTO.setChampion(champion);
                                                return playerMatchDTO;
                                            });
                                })
                )
                .sort((o1, o2) -> (int) -(o1.getMatchId() - o2.getMatchId()));
    }
}
