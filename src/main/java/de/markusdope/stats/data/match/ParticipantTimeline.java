package de.markusdope.stats.data.match;

import lombok.Data;

@Data
public class ParticipantTimeline {
    private StatTotals creepScoreDifference;
    private StatTotals gold;
    private StatTotals experienceDifference;
    private StatTotals creepScore;
    private StatTotals experience;
    private StatTotals damageTakenDifference;
    private StatTotals damageTaken;
}
