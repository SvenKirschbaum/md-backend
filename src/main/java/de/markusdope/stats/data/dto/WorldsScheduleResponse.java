package de.markusdope.stats.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class WorldsScheduleResponse {
    private ElementWrapper[] cargoquery;

    @Data
    public static class ElementWrapper {
        private WorldsScheduleMatch title;
    }

    @Data
    public static class WorldsScheduleMatch {
        @JsonProperty("DateTime UTC")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
        private Instant instant;
        @JsonProperty("Team1")
        private String team1;
        @JsonProperty("Team2")
        private String team2;
    }
}
