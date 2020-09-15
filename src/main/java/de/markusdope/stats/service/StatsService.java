package de.markusdope.stats.service;

import de.markusdope.stats.data.dto.PlayerStats;
import de.markusdope.stats.data.repository.MatchPlayerRepository;
import de.markusdope.stats.data.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private MatchPlayerRepository matchPlayerRepository;

    @Autowired
    private MatchRepository matchRepository;

    public Flux<PlayerStats> getPlayerStats() {
        return
                matchPlayerRepository
                        .findAll()
                        .flatMap(
                                matchPlayer ->
                                        matchRepository
                                                .findById(matchPlayer.getId())
                                                .flatMapIterable(match -> match.getParticipants().stream().map(participant -> Tuples.of(matchPlayer.getPlayers().get(participant.getParticipantId()), participant, participant.getTeam() == match.getBlueTeam().getTeamId() ? match.getBlueTeam() : match.getRedTeam(), match)).collect(Collectors.toSet()))
                        )
                        .groupBy(Tuple4::getT1, tuple4 -> Tuples.of(tuple4.getT2(), tuple4.getT3(), tuple4.getT4()))
                        .flatMap(
                                groupedFlux ->
                                        groupedFlux
                                                .map(PlayerStats::new)
                                                .reduce(PlayerStats::combine)
                                                .doOnNext(
                                                        playerStats ->
                                                                playerStats.setPlayerName(groupedFlux.key())
                                                )
                        );
    }

}
