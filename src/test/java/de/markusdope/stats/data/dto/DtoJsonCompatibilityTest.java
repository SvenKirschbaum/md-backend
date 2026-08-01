package de.markusdope.stats.data.dto;

import de.markusdope.stats.config.JacksonConfig;
import de.markusdope.stats.data.match.Match;
import de.markusdope.stats.data.match.Participant;
import de.markusdope.stats.data.match.ParticipantStats;
import de.markusdope.stats.data.match.Team;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoJsonCompatibilityTest {
    private final JsonMapper mapper = JsonMapper.builder()
            .addModule(new JacksonConfig().jodaModule())
            .build();

    @Test
    void serializesMatchDtoUsingThePermanentJsonContract() throws Exception {
        var players = new LinkedHashMap<Integer, String>();
        players.put(1, "Alice");
        players.put(2, "Bob");
        var dto = new MatchDTO(match(), players, 10);

        JsonNode expected = fixture("/json-contracts/match-dto.json");

        assertThat(serializedTree(dto)).isEqualTo(expected);
    }

    @Test
    void serializesPlayerMatchDtoUsingThePermanentJsonContract() throws Exception {
        var dto = new PlayerMatchDTO();
        dto.setMatchId(42L);
        dto.setWin(true);
        dto.setChampion("Ashe");
        dto.setPlayer(participant());
        dto.setMatchCreationTime(Instant.parse("2020-01-02T03:04:05Z"));
        dto.setMatchDuration(Duration.standardMinutes(30));
        dto.setVersion("10.1.1");

        JsonNode expected = fixture("/json-contracts/player-match-dto.json");

        assertThat(serializedTree(dto)).isEqualTo(expected);
    }

    private static Match match() {
        var match = new Match();
        match.setBlueTeam(team(100, true));
        match.setRedTeam(team(200, false));
        match.setCreationTime(DateTime.parse("2020-01-02T03:04:05Z"));
        match.setDuration(Duration.standardMinutes(30));
        match.setId(42L);
        match.setMap(11);
        match.setQueue(420);
        match.setSeason(10);
        match.setParticipants(List.of(participant()));
        match.setVersion("10.1.1");
        match.setTournamentCode("TOURNAMENT-1");
        match.setMode("CLASSIC");
        match.setPlatform("EUW");
        match.setType("MATCHED_GAME");
        return match;
    }

    private static Participant participant() {
        var participant = new Participant();
        participant.setCurrentPlatform("EUW");
        participant.setPlatform("EUW");
        participant.setHighestTierInSeason("GOLD");
        participant.setSummonerName("Alice");
        participant.setMatchHistoryURI("/matches/Alice");
        participant.setVersion("10.1.1");
        participant.setLane("BOTTOM");
        participant.setRole("DUO_CARRY");
        participant.setCurrentAccountId("current-account");
        participant.setSummonerId("summoner-id");
        participant.setAccountId("account-id");
        participant.setItems(List.of(1001, 3006));
        participant.setStats(stats());
        participant.setSummonerSpellDId(4);
        participant.setSummonerSpellFId(7);
        participant.setChampionId(22);
        participant.setProfileIconId(123);
        participant.setParticipantId(1);
        participant.setTeam(100);
        participant.setPrimaryRunePath(8000);
        participant.setSecondaryRunePath(8300);
        return participant;
    }

    private static ParticipantStats stats() {
        var stats = new ParticipantStats();
        stats.setAssists(3);
        stats.setChampionLevel(18);
        stats.setDeaths(1);
        stats.setGoldEarned(12_345);
        stats.setKills(7);
        stats.setCreepScore(200);
        stats.setDamageDealtToChampions(9_876);
        stats.setVisionScore(42);
        stats.setFirstBloodKiller(true);
        stats.setWinner(true);
        stats.setLongestTimeAlive(Duration.standardMinutes(10));
        stats.setCrowdControlDealt(Duration.millis(2_345));
        stats.setCrowdControlDealtToChampions(Duration.millis(1_234));
        return stats;
    }

    private static Team team(int teamId, boolean winner) {
        var team = new Team();
        team.setBans(List.of(22, 86));
        team.setBaronKills(1);
        team.setDragonKills(3);
        team.setTeamId(teamId);
        team.setFirstBloodKiller(winner);
        team.setFirstTowerKiller(winner);
        team.setWinner(winner);
        team.setPlatform("EUW");
        team.setVersion("10.1.1");
        return team;
    }

    private JsonNode fixture(String path) throws IOException {
        try (var resource = getClass().getResourceAsStream(path)) {
            assertThat(resource).isNotNull();
            return mapper.readTree(resource);
        }
    }

    private JsonNode serializedTree(Object value) throws IOException {
        return mapper.readTree(mapper.writeValueAsBytes(value));
    }
}
