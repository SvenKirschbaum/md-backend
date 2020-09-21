package de.markusdope.stats.data.dto.recordTypes;

import de.markusdope.stats.data.dto.LolRecord;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerRecord<T extends Comparable<T>> extends LolRecord<T> {

    private String player;
    private String lane;
    private int championId;
    private String champion;

    public PlayerRecord(T value, String player, String lane, int championId, String champion, long matchId, boolean inverseSort) {
        super(value, matchId, inverseSort);
        this.player = player;
        this.lane = lane;
        this.championId = championId;
        this.champion = champion;
    }
}
