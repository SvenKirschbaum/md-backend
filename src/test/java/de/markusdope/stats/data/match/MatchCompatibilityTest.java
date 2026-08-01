package de.markusdope.stats.data.match;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchCompatibilityTest {
    private final JsonMapper mapper = JsonMapper.builder().addModule(new JodaModule()).build();

    @Test
    void serializesLikeOriannaCoreMatchData() {
        var oldStats = new com.merakianalytics.orianna.types.data.match.ParticipantStats();
        oldStats.setKills(7);
        oldStats.setCrowdControlDealtToChampions(Duration.millis(1234));
        var oldParticipant = new com.merakianalytics.orianna.types.data.match.Participant();
        oldParticipant.setParticipantId(1);
        oldParticipant.setChampionId(22);
        oldParticipant.setLane("BOTTOM");
        oldParticipant.setStats(oldStats);
        var oldTeam = new com.merakianalytics.orianna.types.data.match.Team();
        oldTeam.setTeamId(100);
        oldTeam.setWinner(true);
        var oldMatch = new com.merakianalytics.orianna.types.data.match.Match();
        oldMatch.setId(42L);
        oldMatch.setCreationTime(new DateTime("2020-01-02T03:04:05Z"));
        oldMatch.setDuration(Duration.standardMinutes(30));
        oldMatch.setVersion("10.1.1");
        oldMatch.setParticipants(List.of(oldParticipant));
        oldMatch.setBlueTeam(oldTeam);

        var stats = new ParticipantStats();
        stats.setKills(7);
        stats.setCrowdControlDealtToChampions(Duration.millis(1234));
        var participant = new Participant();
        participant.setParticipantId(1);
        participant.setChampionId(22);
        participant.setLane("BOTTOM");
        participant.setStats(stats);
        var team = new Team();
        team.setTeamId(100);
        team.setWinner(true);
        var match = new Match();
        match.setId(42L);
        match.setCreationTime(new DateTime("2020-01-02T03:04:05Z"));
        match.setDuration(Duration.standardMinutes(30));
        match.setVersion("10.1.1");
        match.setParticipants(List.of(participant));
        match.setBlueTeam(team);

        assertThat((JsonNode) mapper.valueToTree(match)).isEqualTo(mapper.valueToTree(oldMatch));
    }

    @Test
    void readsLegacyMongoFieldsWithoutMigration() throws Exception {
        var context = new org.springframework.data.mongodb.core.mapping.MongoMappingContext();
        context.afterPropertiesSet();
        var converter = new org.springframework.data.mongodb.core.convert.MappingMongoConverter(
                org.springframework.data.mongodb.core.convert.NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(new org.springframework.data.mongodb.core.convert.MongoCustomConversions(List.of(
                new de.markusdope.stats.util.JodaDateTimeConverter(),
                new de.markusdope.stats.util.JodaDurationConverter())));
        converter.afterPropertiesSet();

        var source = new org.bson.Document("id", 42L)
                .append("creationTime", java.util.Date.from(java.time.Instant.parse("2020-01-02T03:04:05Z")))
                .append("duration", new org.bson.Document("iMillis", 1_800_000L))
                .append("participants", List.of(new org.bson.Document("participantId", 1)
                        .append("championId", 22)
                        .append("stats", new org.bson.Document("kills", 7)
                                .append("crowdControlDealtToChampions", new org.bson.Document("iMillis", 1234L)))))
                .append("blueTeam", new org.bson.Document("teamId", 100).append("winner", true));

        Match match = converter.read(Match.class, source);

        assertThat(match.getId()).isEqualTo(42L);
        assertThat(match.getCreationTime().toInstant().getMillis()).isEqualTo(1_577_934_245_000L);
        assertThat(match.getDuration()).isEqualTo(Duration.standardMinutes(30));
        assertThat(match.getParticipants().getFirst().getStats().getCrowdControlDealtToChampions())
                .isEqualTo(Duration.millis(1234));
        assertThat(match.getBlueTeam().isWinner()).isTrue();
    }
}
