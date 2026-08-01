package de.markusdope.stats.data.dto;

import de.markusdope.stats.data.document.MatchDocument;
import de.markusdope.stats.data.document.MatchPlayer;
import de.markusdope.stats.data.dto.recordTypes.PlayerRecord;
import de.markusdope.stats.data.match.Event;
import de.markusdope.stats.data.match.Match;
import de.markusdope.stats.data.match.Participant;
import de.markusdope.stats.data.match.ParticipantStats;
import de.markusdope.stats.data.match.Team;
import org.joda.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StatsCompatibilityTest {
    @Test
    void preservesPlayerTotalsAndRecordFields() {
        Fixture fixture = fixture();

        var playerStats = new PlayerStats(Tuples.of(fixture.ashe(), fixture.blueTeam(), fixture.match()));
        assertThat(playerStats.getGameDuration()).isEqualTo(1800);
        assertThat(playerStats.getWins()).isEqualTo(1);
        assertThat(playerStats.getKills()).isEqualTo(7);

        var records = LolRecordsDTO.ofMatchDocument(
                fixture.document(), fixture.players(), Map.of(22, "Ashe", 86, "Garen"));
        var killRecord = (PlayerRecord<?>) records.getRecords().get("kills").iterator().next();
        assertThat(killRecord.getChampion()).isEqualTo("Ashe");
        assertThat(killRecord.getPlayer()).isEqualTo("Alice");
        assertThat(killRecord.getMatchId()).isEqualTo(42L);
        assertThat(records.getRecords()).containsKeys("earlyKill", "earlyDeath");
    }

    @Test
    void treatsMissingLegacyTimelineAsNoEvents() {
        Fixture fixture = fixture();
        fixture.document().setTimeline(null);

        var records = records(fixture);

        assertThat(records.getRecords()).containsKey("kills");
        assertThat(records.getRecords()).doesNotContainKeys("earlyKill", "earlyDeath");
    }

    @Test
    void combinesRecordsWhenOnlyOneMatchHasFirstKillEvents() {
        Fixture fixture = fixture();
        var withFirstKill = records(fixture);
        fixture.document().setTimeline(List.of());
        var withoutFirstKill = records(fixture);

        var combined = LolRecordsDTO.combine(withFirstKill, withoutFirstKill);

        assertThat(combined.getRecords()).containsKeys("kills", "earlyKill", "earlyDeath");

        fixture.document().setTimeline(List.of());
        withoutFirstKill = records(fixture);
        fixture.document().setTimeline(List.of(List.of(firstKill())));
        withFirstKill = records(fixture);

        var reverseCombined = LolRecordsDTO.combine(withoutFirstKill, withFirstKill);

        assertThat(reverseCombined.getRecords()).containsKeys("kills", "earlyKill", "earlyDeath");
    }

    @Test
    void readsLegacyRootDocumentAndTimelineWithoutMigration() throws Exception {
        var context = new org.springframework.data.mongodb.core.mapping.MongoMappingContext();
        context.afterPropertiesSet();
        var converter = new org.springframework.data.mongodb.core.convert.MappingMongoConverter(
                org.springframework.data.mongodb.core.convert.NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(new org.springframework.data.mongodb.core.convert.MongoCustomConversions(List.of(
                new de.markusdope.stats.util.JodaDateTimeConverter(),
                new de.markusdope.stats.util.JodaDurationConverter())));
        converter.afterPropertiesSet();

        var event = new org.bson.Document("type", "CHAMPION_KILL")
                .append("killerId", 1).append("victimId", 2)
                .append("timestamp", new org.bson.Document("iMillis", 120_000L));
        var source = new org.bson.Document("_class", MatchDocument.class.getName())
                .append("_id", 42L).append("season", 1)
                .append("match", new org.bson.Document("_id", 43L).append("version", "10.1.1"))
                .append("timeline", List.of(List.of(event)));

        MatchDocument document = converter.read(MatchDocument.class, source);

        assertThat(document.getId()).isEqualTo(42L);
        assertThat(document.getMatch().getId()).isEqualTo(43L);
        assertThat(document.getTimeline().getFirst().getFirst().getType()).isEqualTo("CHAMPION_KILL");
        assertThat(document.getTimeline().getFirst().getFirst().getTimestamp())
                .isEqualTo(Duration.standardMinutes(2));
    }

    private static Fixture fixture() {
        ParticipantStats asheStats = new ParticipantStats();
        asheStats.setKills(7);
        asheStats.setAssists(3);
        asheStats.setDeaths(1);
        asheStats.setCrowdControlDealtToChampions(Duration.ZERO);
        Participant ashe = new Participant();
        ashe.setParticipantId(1);
        ashe.setChampionId(22);
        ashe.setLane("BOTTOM");
        ashe.setTeam(100);
        ashe.setStats(asheStats);

        ParticipantStats garenStats = new ParticipantStats();
        garenStats.setKills(0);
        garenStats.setDeaths(7);
        garenStats.setCrowdControlDealtToChampions(Duration.ZERO);
        Participant garen = new Participant();
        garen.setParticipantId(2);
        garen.setChampionId(86);
        garen.setLane("TOP");
        garen.setTeam(200);
        garen.setStats(garenStats);

        Team blue = new Team();
        blue.setTeamId(100);
        blue.setWinner(true);
        Team red = new Team();
        red.setTeamId(200);
        Match match = new Match();
        match.setId(42L);
        match.setVersion("10.1.1");
        match.setDuration(Duration.standardMinutes(30));
        match.setBlueTeam(blue);
        match.setRedTeam(red);
        match.setParticipants(List.of(ashe, garen));

        MatchDocument document = new MatchDocument();
        document.setId(42L);
        document.setSeason(1);
        document.setMatch(match);
        document.setTimeline(List.of(List.of(firstKill())));

        MatchPlayer players = new MatchPlayer();
        players.setId(42L);
        players.setPlayers(new MatchPlayer.Player[]{
                new MatchPlayer.Player(1, "Alice"), new MatchPlayer.Player(2, "Bob")});
        return new Fixture(match, ashe, blue, document, players);
    }

    private static LolRecordsDTO records(Fixture fixture) {
        return LolRecordsDTO.ofMatchDocument(
                fixture.document(), fixture.players(), Map.of(22, "Ashe", 86, "Garen"));
    }

    private static Event firstKill() {
        Event firstKill = new Event();
        firstKill.setType("CHAMPION_KILL");
        firstKill.setKillerId(1);
        firstKill.setVictimId(2);
        firstKill.setTimestamp(Duration.standardMinutes(2));
        return firstKill;
    }

    private record Fixture(Match match, Participant ashe, Team blueTeam,
                           MatchDocument document, MatchPlayer players) {
    }
}
