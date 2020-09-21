package de.markusdope.stats.data.dto.record;

import lombok.Data;

@Data
public class Percent implements Comparable<Percent> {
    final private double value;

    @Override
    public int compareTo(Percent o) {
        return Double.compare(this.value, o.getValue());
    }

    @Override
    public String toString() {
        return String.format("%.2f%%", this.value * 100);
    }
}
