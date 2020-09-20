package de.markusdope.stats.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.data.match.Event;
import com.merakianalytics.orianna.types.data.match.Match;
import com.merakianalytics.orianna.types.data.match.Participant;
import de.markusdope.stats.data.document.MatchDocument;
import de.markusdope.stats.data.document.MatchPlayer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Data
public class LolRecords {

    private Map<String, Set<LolRecord>> records;

    public static LolRecords ofMatchDocument(MatchDocument matchDocument, MatchPlayer matchPlayer) {
        Match match = matchDocument.getMatch();
        LolRecords lolRecords = new LolRecords();
        Map<String, Set<LolRecord>> records = new LinkedHashMap<>();

        BiFunction<Function<Participant, Comparable>, Boolean, Set<LolRecord>> createParticipantRecord = (participantComparableFunction, inverse) -> {
            return match.getParticipants()
                    .stream()
                    .map(participant ->
                            new LolRecord(
                                    participantComparableFunction.apply(participant),
                                    matchPlayer.getParticipant(participant.getParticipantId()),
                                    participant.getLane(),
                                    participant.getChampionId(),
                                    Orianna.championWithId(participant.getChampionId()).get().getName(),
                                    match.getId(),
                                    inverse
                            )
                    )
                    .map(Set::of)
                    .reduce(LolRecords::getMaxSet)
                    .orElseGet(Collections::emptySet);
        };

        records.put("kills", createParticipantRecord.apply(participant -> participant.getStats().getKills(), false));
        records.put("deaths", createParticipantRecord.apply(participant -> participant.getStats().getDeaths(), false));
        records.put("assists", createParticipantRecord.apply(participant -> participant.getStats().getAssists(), false));
        records.put("kda", createParticipantRecord.apply(participant -> new KDA(participant.getStats().getKills(), participant.getStats().getDeaths(), participant.getStats().getAssists()), false));
        records.put("gold", createParticipantRecord.apply(participant -> participant.getStats().getGoldEarned(), false));
        records.put("cs", createParticipantRecord.apply(participant -> participant.getStats().getCreepScore(), false));
        records.put("visionScore", createParticipantRecord.apply(participant -> participant.getStats().getVisionScore(), false));

        Optional<Event> first_champion_kill_opt = Arrays.stream(matchDocument.getTimeline()).flatMap(Collection::stream).filter(event -> event.getType().equals("CHAMPION_KILL")).min(Comparator.comparing(Event::getTimestamp));

        if (first_champion_kill_opt.isPresent()) {
            Event first_champion_kill = first_champion_kill_opt.get();
            Participant participant = getParticipant(match, first_champion_kill.getVictimId());

            records.put("earlyKill",
                    Collections.singleton(
                            new LolRecord<TimeRecord>(
                                    new TimeRecord(Duration.ofMillis(first_champion_kill.getTimestamp().getMillis())),
                                    matchPlayer.getParticipant(first_champion_kill.getVictimId()),
                                    participant.getLane(),
                                    participant.getChampionId(),
                                    Orianna.championWithId(participant.getChampionId()).get().getName(),
                                    match.getId(),
                                    true
                            )
                    )
            );
        }

        lolRecords.setRecords(records);
        return lolRecords;
    }

    public static LolRecords combine(LolRecords e1, LolRecords e2) {
        LolRecords lolRecords = new LolRecords();
        Map<String, Set<LolRecord>> records = new LinkedHashMap<>();

        e1.getRecords().forEach(
                (key, lolRecord) -> {
                    records.put(key, LolRecords.getMaxSet(lolRecord, e2.getRecords().get(key)));
                    e2.getRecords().remove(key);
                }
        );
        //In case e2 contains record entries which e1 doesnt contain
        e2.getRecords().forEach(
                (key, lolRecord) -> {
                    records.put(key, LolRecords.getMaxSet(lolRecord, e1.getRecords().get(key)));
                }
        );

        lolRecords.setRecords(records);
        return lolRecords;
    }

    private static Participant getParticipant(Match match, int participantId) {
        return match.getParticipants().stream().filter(participant -> participant.getParticipantId() == participantId).findFirst().get();
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
        private boolean inverseSort = false;

        @Override
        public int compareTo(LolRecord<T> o) {
            return inverseSort ? -this.value.compareTo(o.getValue()) : this.value.compareTo(o.getValue());
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

    @Data
    public static class TimeRecord implements Comparable<TimeRecord> {
        final private Duration duration;

        @Override
        public int compareTo(TimeRecord o) {
            return duration.compareTo(o.getDuration());
        }

        @Override
        public String toString() {
            if (this.duration.toMinutes() > 0) {
                return String.format("%d Minuten %d Sekunden", this.duration.toMinutes(), this.duration.toSecondsPart());
            }
            return String.format("%d Sekunden", this.duration.toSecondsPart());
        }
    }
}
