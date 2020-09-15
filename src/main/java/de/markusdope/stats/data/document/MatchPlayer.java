package de.markusdope.stats.data.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
@Data
public class MatchPlayer {
    Map<Integer, String> players;
    @Id
    private long id;
}
