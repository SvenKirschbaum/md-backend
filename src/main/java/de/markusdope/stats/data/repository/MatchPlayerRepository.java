package de.markusdope.stats.data.repository;

import de.markusdope.stats.data.document.MatchPlayer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchPlayerRepository extends ReactiveCrudRepository<MatchPlayer, Long> {
}
