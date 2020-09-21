package de.markusdope.stats.data.dto.record;

import lombok.Data;

import java.time.Duration;

@Data
public class TimeRecord implements Comparable<TimeRecord> {
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
