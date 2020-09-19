package de.markusdope.stats.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.merakianalytics.orianna.types.data.match.Match;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResponseDTO {
    private final String message;
    private final Long matchId;
    private Match match;
    private Map<Integer, String> playerMapping;
    private Set<String> knownPlayers;
}
