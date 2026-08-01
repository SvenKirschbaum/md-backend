package de.markusdope.stats.data.document;

import de.markusdope.stats.data.match.Event;
import de.markusdope.stats.data.match.Match;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class MatchDocument {
    private Long id;
    @Indexed
    private Integer season;
    private Match match;
    private List<List<Event>> timeline;
}
