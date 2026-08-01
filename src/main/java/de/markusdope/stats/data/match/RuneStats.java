package de.markusdope.stats.data.match;

import lombok.Data;

import java.util.List;

@Data
public class RuneStats {
    private int id;
    private String platform;
    private String version;
    private List<Integer> variables;
}
