package de.markusdope.stats.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.data.match.Match;
import com.merakianalytics.orianna.types.data.match.Participant;
import de.markusdope.stats.data.document.MatchPlayer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.function.Function;

@Data
public class LolRecords {

    private Map<String, Set<LolRecord>> records;

    public static LolRecords ofMatch(Match match, MatchPlayer matchPlayer) {
        LolRecords lolRecords = new LolRecords();
        Map<String, Set<LolRecord>> records = new LinkedHashMap<>();

        Function<Function<Participant, Comparable>, Set<LolRecord>> createParticipantRecord = participantComparableFunction -> {
            return match.getParticipants()
                    .stream()
                    .map(participant ->
                            new LolRecord(
                                    participantComparableFunction.apply(participant),
                                    matchPlayer.getPlayers().get(participant.getParticipantId()),
                                    participant.getLane(),
                                    participant.getChampionId(),
                                    Orianna.championWithId(participant.getChampionId()).get().getName(),
                                    match.getId()
                            )
                    )
                    .map(Set::of)
                    .reduce(LolRecords::getMaxSet)
                    .orElseGet(Collections::emptySet);
        };

        records.put("kills", createParticipantRecord.apply(participant -> participant.getStats().getKills()));
        records.put("deaths", createParticipantRecord.apply(participant -> participant.getStats().getDeaths()));
        records.put("assists", createParticipantRecord.apply(participant -> participant.getStats().getAssists()));
        records.put("kda", createParticipantRecord.apply(participant -> new KDA(participant.getStats().getKills(), participant.getStats().getDeaths(), participant.getStats().getAssists())));
        records.put("gold", createParticipantRecord.apply(participant -> participant.getStats().getGoldEarned()));
        records.put("cs", createParticipantRecord.apply(participant -> participant.getStats().getCreepScore()));

        lolRecords.setRecords(records);
        return lolRecords;
    }

    public static LolRecords combine(LolRecords e1, LolRecords e2) {
        LolRecords lolRecords = new LolRecords();
        Map<String, Set<LolRecord>> records = new LinkedHashMap<>();

        e1.getRecords().forEach(
                (key, lolRecord) -> {
                    records.put(key, LolRecords.getMaxSet(lolRecord, e2.getRecords().get(key)));
                }
        );

        lolRecords.setRecords(records);
        return lolRecords;
    }

    private static Set<LolRecord> getMaxSet(Set<LolRecord> e1, Set<LolRecord> e2) {
        int comparison = e1.iterator().next().compareTo(e2.iterator().next());
        Set<LolRecord> r = new HashSet<>();
        if (comparison == 0) {
            r.addAll(e1);
            r.addAll(e2);
            return r;
        } else if (comparison > 0) {
            r.addAll(e1);
            return r;
        } else {
            r.addAll(e2);
            return r;
        }
    }

    @AllArgsConstructor
    @Data
    public static class LolRecord<T extends Comparable<T>> implements Comparable<LolRecord<T>> {
        @JsonIgnore
        private T value;
        private String player;
        private String lane;
        private int championId;
        private String champion;
        private long matchId;

        @Override
        public int compareTo(LolRecord<T> o) {
            return this.value.compareTo(o.getValue());
        }

        @JsonInclude
        @JsonProperty("value")
        public String getValueString() {
            return value.toString();
        }
    }

    @Data
    public static class KDA implements Comparable<KDA> {

        private final int kills, deaths, assists;
        private final Double kda;

        public KDA(int kills, int deaths, int assists) {
            this.kills = kills;
            this.deaths = deaths;
            this.assists = assists;
            this.kda = (kills + assists) / (double) deaths;
        }

        @Override
        public int compareTo(KDA o) {
            int c = this.kda.compareTo(o.getKda());

            if (c == 0 && Double.POSITIVE_INFINITY == this.kda) {
                return Integer.compare(this.getKills() + this.getAssists(), o.getKills() + o.getAssists());
            }

            return c;
        }

        @Override
        public String toString() {
            return String.format("%d/%d/%d", this.kills, this.deaths, this.assists);
        }
    }
}
