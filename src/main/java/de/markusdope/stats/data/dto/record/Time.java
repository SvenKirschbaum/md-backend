package de.markusdope.stats.data.dto.record;

import lombok.Data;

import java.time.Duration;

@Data
public class Time implements Comparable<Time> {
    final private Duration duration;

    @Override
    public int compareTo(Time o) {
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
