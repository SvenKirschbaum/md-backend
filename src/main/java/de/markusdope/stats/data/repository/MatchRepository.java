package de.markusdope.stats.data.repository;

import com.merakianalytics.orianna.types.data.match.Match;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends ReactiveCrudRepository<Match, Long> {
}
