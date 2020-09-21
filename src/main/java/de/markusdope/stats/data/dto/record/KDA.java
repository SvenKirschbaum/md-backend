package de.markusdope.stats.data.dto.record;

import lombok.Data;

@Data
public class KDA implements Comparable<KDA> {

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
