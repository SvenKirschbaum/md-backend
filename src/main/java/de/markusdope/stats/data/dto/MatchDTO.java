package de.markusdope.stats.data.dto;

import com.merakianalytics.orianna.types.data.match.Match;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class MatchDTO {
    private Match match;
    private Map<Integer, String> players;
    private Integer season;
}
