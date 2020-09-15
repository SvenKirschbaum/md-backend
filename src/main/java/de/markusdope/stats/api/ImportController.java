package de.markusdope.stats.api;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.GameType;
import com.merakianalytics.orianna.types.core.match.Participant;
import com.merakianalytics.orianna.types.data.match.Match;
import de.markusdope.stats.data.document.MatchPlayer;
import de.markusdope.stats.data.dto.ImportRequestDTO;
import de.markusdope.stats.data.dto.ImportResponseDTO;
import de.markusdope.stats.data.repository.MatchPlayerRepository;
import de.markusdope.stats.data.repository.MatchRepository;
import de.markusdope.stats.exception.UnprocessableEntityException;
import de.markusdope.stats.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/import")
@PreAuthorize("hasRole('manager')")
public class ImportController {

    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private MatchPlayerRepository matchPlayerRepository;
    @Autowired
    private PlayerService playerService;

    @GetMapping("/{matchId}")
    public Mono<ResponseEntity<ImportResponseDTO>> getAction(@PathVariable Long matchId) {
        return Mono.just(matchId)
                .publishOn(Schedulers.boundedElastic())
                .map(id -> Orianna.matchWithId(id).get())
                .flatMap(match -> {
                    if (!match.getType().equals(GameType.CUSTOM_GAME))
                        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ImportResponseDTO("The Match is not a custom game", matchId)));

                    return Mono.just(match)
                            .map(match1 -> {
                                ImportResponseDTO importResponseDTO = new ImportResponseDTO("The match has been sucessfully loaded", matchId);
                                importResponseDTO.setBlueTeam(match1.getBlueTeam().getParticipants().toArray(Participant[]::new));
                                importResponseDTO.setRedTeam(match1.getRedTeam().getParticipants().toArray(Participant[]::new));
                                return importResponseDTO;
                            })
                            .flatMap(importResponseDTO -> {
                                return matchPlayerRepository.findById(matchId).map(matchPlayer -> {
                                    importResponseDTO.setPlayerMapping(matchPlayer.getPlayers());
                                    return importResponseDTO;
                                }).defaultIfEmpty(importResponseDTO);
                            })
                            .flatMap(importResponseDTO -> {
                                return playerService.getKnownPlayers().collect(Collectors.toSet()).map(strings -> {
                                    importResponseDTO.setKnownPlayers(strings);
                                    return importResponseDTO;
                                }).defaultIfEmpty(importResponseDTO);
                            })
                            .map(importResponseDTO ->
                                    ResponseEntity.status(HttpStatus.OK).body(importResponseDTO)
                            );
                });
    }

    @PostMapping("/{matchId}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<Void> postAction(@PathVariable Long matchId, @RequestBody ImportRequestDTO importRequestDTO) {
        return Mono.just(matchId)
                .publishOn(Schedulers.boundedElastic())
                .map(id -> Orianna.matchWithId(id).get())
                .flatMap(match2 -> {
                    if (!match2.getType().equals(GameType.CUSTOM_GAME))
                        return Mono.error(new UnprocessableEntityException());
                    return Mono.just(match2)
                            .flatMap(match -> matchRepository.save(match.getCoreData()))
                            .flatMapIterable(Match::getParticipants)
                            .map(com.merakianalytics.orianna.types.data.match.Participant::getParticipantId)
                            .map(participantId -> Tuples.of(participantId, Objects.requireNonNull(importRequestDTO.getPlayerMapping().get(participantId))))
                            .collectMap(Tuple2::getT1, Tuple2::getT2)
                            .map(playerMap -> {
                                MatchPlayer matchPlayer = new MatchPlayer();
                                matchPlayer.setId(matchId);
                                matchPlayer.setPlayers(playerMap);
                                return matchPlayer;
                            })
                            .flatMap(matchPlayerRepository::save)
                            .then();
                });

    }
}
