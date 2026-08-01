package de.markusdope.stats.data.match;

import org.joda.time.Duration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchCompatibilityTest {
    @Test
    void exposesTheCharacterizedMatchProperties() throws Exception {
        assertThat(beanProperties(Match.class)).containsExactlyInAnyOrder(
                "blueTeam", "creationTime", "duration", "id", "map", "mode", "participants",
                "platform", "queue", "redTeam", "season", "tournamentCode", "type", "version");
        assertThat(beanProperties(Participant.class)).containsExactlyInAnyOrder(
                "accountId", "championId", "currentAccountId", "currentPlatform", "highestTierInSeason",
                "items", "lane", "matchHistoryURI", "participantId", "platform", "primaryRunePath",
                "profileIconId", "role", "runeStats", "secondaryRunePath", "stats", "summonerId",
                "summonerName", "summonerSpellDId", "summonerSpellFId", "team", "timeline", "version");
        assertThat(beanProperties(Team.class)).containsExactlyInAnyOrder(
                "bans", "baronKills", "dominionScore", "dragonKills", "firstBaronKiller",
                "firstBloodKiller", "firstDragonKiller", "firstInhibitorKiller", "firstRiftHeraldKiller",
                "firstTowerKiller", "inhibitorKills", "platform", "riftHeraldKills", "teamId",
                "towerKills", "version", "vilemawKills", "winner");
    }

    private static java.util.Set<String> beanProperties(Class<?> type) throws Exception {
        return java.util.Arrays.stream(java.beans.Introspector.getBeanInfo(type).getPropertyDescriptors())
                .map(java.beans.PropertyDescriptor::getName)
                .filter(name -> !"class".equals(name))
                .collect(java.util.stream.Collectors.toSet());
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

        var source = new org.bson.Document("_id", 42L)
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

    @Test
    void writesMatchIdUsingMongoIdConvention() throws Exception {
        var context = new org.springframework.data.mongodb.core.mapping.MongoMappingContext();
        context.afterPropertiesSet();
        var converter = new org.springframework.data.mongodb.core.convert.MappingMongoConverter(
                org.springframework.data.mongodb.core.convert.NoOpDbRefResolver.INSTANCE, context);
        converter.afterPropertiesSet();
        var match = new Match();
        match.setId(42L);
        var target = new org.bson.Document();

        converter.write(match, target);

        assertThat(target).containsEntry("_id", 42L).doesNotContainKey("id");
    }
}
