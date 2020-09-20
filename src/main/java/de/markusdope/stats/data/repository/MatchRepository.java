package de.markusdope.stats.data.repository;

import de.markusdope.stats.data.document.MatchDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends ReactiveCrudRepository<MatchDocument, Long> {
}
