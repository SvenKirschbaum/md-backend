package de.markusdope.stats.data.match;

import lombok.Data;

import java.util.List;

@Data
public class Team {
    private List<Integer> bans;
    private int baronKills, riftHeraldKills, vilemawKills, inhibitorKills, towerKills, dominionScore, dragonKills, teamId;
    private boolean firstDragonKiller, firstInhibitorKiller, firstRiftHeraldKiller, firstBaronKiller;
    private boolean firstBloodKiller, firstTowerKiller, winner;
    private String platform, version;
}
