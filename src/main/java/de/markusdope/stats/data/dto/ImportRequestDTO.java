package de.markusdope.stats.data.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ImportRequestDTO {
    Map<Integer, String> playerMapping;
}
