package de.markusdope.stats.data.match;

import lombok.Data;

import java.util.List;

@Data
public class Participant {
    private String currentPlatform;
    private String platform;
    private String highestTierInSeason;
    private String summonerName;
    private String matchHistoryURI;
    private String version;
    private String lane;
    private String role;
    private String currentAccountId;
    private String summonerId;
    private String accountId;
    private List<Integer> items;
    private List<RuneStats> runeStats;
    private ParticipantStats stats;
    private int summonerSpellDId;
    private int summonerSpellFId;
    private int championId;
    private int profileIconId;
    private int participantId;
    private int team;
    private int primaryRunePath;
    private int secondaryRunePath;
    private ParticipantTimeline timeline;
}
