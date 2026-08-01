package de.markusdope.stats.data.match;

import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;

@Data
public class Match {
    private Team blueTeam;
    private Team redTeam;
    private DateTime creationTime;
    private Duration duration;
    private long id;
    private int map;
    private int queue;
    private int season;
    private List<Participant> participants;
    private String version;
    private String tournamentCode;
    private String mode;
    private String platform;
    private String type;
}
