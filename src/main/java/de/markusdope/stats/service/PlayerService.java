package de.markusdope.stats.service;

import de.markusdope.stats.data.document.MatchPlayer;
import de.markusdope.stats.data.repository.MatchPlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;

@Service
public class PlayerService {
    @Autowired
    private MatchPlayerRepository matchPlayerRepository;

    public Flux<String> getKnownPlayers() {
        return matchPlayerRepository
                .findAll()
                .flatMapIterable(matchPlayer -> Arrays.asList(matchPlayer.getPlayers()))
                .map(MatchPlayer.Player::getName)
                .distinct();
    }
}
