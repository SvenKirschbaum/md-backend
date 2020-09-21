package de.markusdope.stats.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, property = "type", use = JsonTypeInfo.Id.NAME)
public abstract class LolRecord<T extends Comparable<T>> implements Comparable<LolRecord<T>> {
    @JsonIgnore
    private T value;
    private long matchId;
    @JsonIgnore
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
