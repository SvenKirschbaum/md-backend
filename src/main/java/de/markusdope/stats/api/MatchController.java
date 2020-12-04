package de.markusdope.stats.api;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.data.match.Match;
import com.merakianalytics.orianna.types.data.match.Participant;
import de.markusdope.stats.data.document.MatchPlayer;
import de.markusdope.stats.data.dto.MatchDTO;
import de.markusdope.stats.data.dto.PlayerMatchDTO;
import de.markusdope.stats.data.repository.MatchPlayerRepository;
import de.markusdope.stats.data.repository.MatchRepository;
import de.markusdope.stats.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

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
                .map(matchPlayer -> matchPlayer.getPlayers())
                .map(players -> {
                    Map<Integer, String> playerMap = new HashMap<>();
                    for (MatchPlayer.Player p : players) {
                        playerMap.put(p.getParticipantId(), p.getName());
                    }
                    return playerMap;
                })
                .flatMap(playerMap -> matchRepository.findById(id).map(match -> new MatchDTO(match.getMatch(), playerMap)))
                .switchIfEmpty(Mono.error(new NotFoundException()));
    }


    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/{id}")
    public Mono<Void> deleteMatchAction(@PathVariable Long id) {
        return matchPlayerRepository
                .deleteById(id)
                .and(matchRepository.deleteById(id));
    }

    @GetMapping("/player/{name}")
    public Flux<PlayerMatchDTO> getPlayerMatches(@PathVariable String name) {
        return matchPlayerRepository
                .findAllByPlayer(name)
                .flatMap(matchPlayer ->
                        matchRepository
                                .findById(matchPlayer.getId())
                                .map(matchDocument -> {
                                    Match match = matchDocument.getMatch();
                                    Integer participantId = matchPlayer.getParticipant(name);

                                    PlayerMatchDTO playerMatchDTO = new PlayerMatchDTO();
                                    playerMatchDTO.setMatchId(match.getId());

                                    Participant p = null;
                                    for (Participant c : match.getParticipants()) {
                                        if (c.getParticipantId() == participantId) p = c;
                                    }

                                    if (p.getTeam() == match.getBlueTeam().getTeamId()) {
                                        playerMatchDTO.setWin(match.getBlueTeam().isWinner());
                                    } else {
                                        playerMatchDTO.setWin(match.getRedTeam().isWinner());
                                    }

                                    playerMatchDTO.setPlayer(p);

                                    playerMatchDTO.setChampion(Orianna.championWithId(p.getChampionId()).get().getName());
                                    playerMatchDTO.setMatchCreationTime(match.getCreationTime().toInstant());
                                    playerMatchDTO.setMatchDuration(match.getDuration());
                                    playerMatchDTO.setVersion(match.getVersion());


                                    return playerMatchDTO;
                                })
                )
                .sort((o1, o2) -> (int) -(o1.getMatchId() - o2.getMatchId()));
    }
}
