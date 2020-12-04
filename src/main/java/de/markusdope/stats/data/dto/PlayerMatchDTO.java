package de.markusdope.stats.data.dto;

import com.merakianalytics.orianna.types.data.match.Participant;
import lombok.Data;
import org.joda.time.Duration;
import org.joda.time.Instant;

@Data
public class PlayerMatchDTO {
    private Long matchId;
    private boolean win;
    private String champion;
    private Participant player;
    private Instant matchCreationTime;
    private Duration matchDuration;
    private String version;
}
