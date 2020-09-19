package de.markusdope.stats.data.repository;

import de.markusdope.stats.data.document.MatchPlayer;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MatchPlayerRepository extends ReactiveMongoRepository<MatchPlayer, Long> {

    @Query("{ 'players.name' : ?0 }")
    public Flux<MatchPlayer> findAllByPlayer(String playername);
}
