package de.markusdope.stats.data.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class MatchPlayer {
    @Id
    private Long id;
    private Player[] players;

    public String getParticipant(int participantId) {
        for (Player player : this.players) {
            if (player.getParticipantId() == participantId) return player.getName();
        }
        return null;
    }

    public Integer getParticipant(String participantName) {
        for (Player player : this.players) {
            if (player.getName().equals(participantName)) return player.getParticipantId();
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    public static class Player {
        private Integer participantId;
        private String name;
    }
}
