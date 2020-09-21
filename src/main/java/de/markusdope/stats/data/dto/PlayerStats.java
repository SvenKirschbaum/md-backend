package de.markusdope.stats.data.dto;

import com.merakianalytics.orianna.types.data.match.Match;
import com.merakianalytics.orianna.types.data.match.Participant;
import com.merakianalytics.orianna.types.data.match.Team;
import lombok.Data;
import reactor.util.function.Tuple3;

@Data
public class PlayerStats {

    private String playerName;

    private long gameDuration;

    private int wins;
    private int losses;

    private int kills;
    private int deaths;
    private int assists;

    private long damage;
    private long gold;
    private long cs;

    private int visionScore;

    public PlayerStats() {

    }

    public PlayerStats(Tuple3<Participant, Team, Match> tuple) {
        Participant participant = tuple.getT1();
        Team team = tuple.getT2();
        Match match = tuple.getT3();

        this.setGameDuration(match.getDuration().getStandardSeconds());

        this.setWins(team.isWinner() ? 1 : 0);
        this.setLosses(team.isWinner() ? 0 : 1);

        this.setKills(participant.getStats().getKills());
        this.setDeaths(participant.getStats().getDeaths());
        this.setAssists(participant.getStats().getAssists());

        this.setDamage(participant.getStats().getDamageDealt());
        this.setGold(participant.getStats().getGoldEarned());
        this.setCs(participant.getStats().getCreepScore() + participant.getStats().getNeutralMinionsKilled());

        this.setVisionScore(participant.getStats().getVisionScore());
    }

    public static PlayerStats combine(PlayerStats p1, PlayerStats p2) {
        PlayerStats stats = new PlayerStats();

        stats.setGameDuration(p1.getGameDuration() + p2.getGameDuration());

        stats.setWins(p1.getWins() + p2.getWins());
        stats.setLosses(p1.getLosses() + p2.getLosses());

        stats.setKills(p1.getKills() + p2.getKills());
        stats.setDeaths(p1.getDeaths() + p2.getDeaths());
        stats.setAssists(p1.getAssists() + p2.getAssists());

        stats.setDamage(p1.getDamage() + p2.getDamage());
        stats.setGold(p1.getGold() + p2.getGold());
        stats.setCs(p1.getCs() + p2.getCs());

        stats.setVisionScore(p1.getVisionScore() + p2.getVisionScore());

        return stats;
    }
}
