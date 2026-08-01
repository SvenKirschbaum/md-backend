package de.markusdope.stats.data.match;

import lombok.Data;
import org.joda.time.Duration;

@Data
public class ParticipantStats {
    private int altarsCaptured, altarsNeutralized, assists, championLevel, combatScore, deaths, doubleKills, goldEarned, goldSpent, inhibitorKills;
    private int killingSprees, kills, largestCriticalStrike, largestKillingSpree, largestMultiKill, neutralMinionsKilled;
    private int neutralMinionsKilledInEnemyJungle, neutralMinionsKilledInAllyJungle, nodesCaptured, nodeCaptureAssists;
    private int nodesNeutralized, nodeNeutralizeAssists, objectiveScore, pentaKills, quadraKills, greenWardsPurchased;
    private int teamObjectives, creepScore, score, scoreRank, unitsHealed, tripleKills, turretKills, hexaKills;
    private int pinkWardsPurchased, wardsKilled, wardsPlaced, damageToObjectives, damageToTurrets, damageMitigated;
    private int magicDamageTaken, magicDamageDealt, magicDamageDealtToChampions, physicalDamageDealt;
    private int physicalDamageDealtToChampions, physicalDamageTaken, damageDealt, damageDealtToChampions;
    private int damageTaken, damageHealed, trueDamageDealt, trueDamageDealtToChampions, trueDamageTaken, visionScore;
    private int playerScore0, playerScore1, playerScore2, playerScore3, playerScore4;
    private int playerScore5, playerScore6, playerScore7, playerScore8, playerScore9;
    private boolean firstBloodAssistant, firstBloodKiller, firstInhibitorKillAssistant, firstInhibitorKiller;
    private boolean firstTowerKillAssistant, firstTowerKiller, winner;
    private Duration longestTimeAlive, crowdControlDealt, crowdControlDealtToChampions;
}
