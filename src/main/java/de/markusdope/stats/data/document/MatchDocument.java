package de.markusdope.stats.data.document;

import com.merakianalytics.orianna.types.data.match.Frame;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class MatchDocument {
    private Long id;
    @Indexed
    private Integer season;
    private com.merakianalytics.orianna.types.data.match.Match match;
    private Frame[] timeline;
}