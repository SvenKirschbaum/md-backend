package de.markusdope.stats.data.match;

import lombok.Data;
import org.joda.time.Duration;

import java.util.List;

@Data
public class Event {
    private String ascensionType, buildingType, capturedPoint, laneType, levelUpType;
    private String monsterSubType, monsterType, turretType, type, wardType;
    private List<Integer> assistingParticipants;
    private int killerId, victimId, afterId, itemId, participantId, creatorId, beforeId, team, skill;
    private Position position;
    private Duration timestamp;
}
