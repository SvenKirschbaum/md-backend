package de.markusdope.stats.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "markusdope")
@Data
public class MarkusDopeStatsProperties {
    /**
     * Resource ID from where additional Roles are extracted from provided JWT Tokens
     */
    private String oauthResourceId;

    /**
     * API Key for Riot APIs
     */
    private String riotApiKey;

    /**
     * The currently active Season
     */
    private Integer currentSeason;

    /**
     * Username for lol.fandom.com
     */
    private String lolFandomUsername;
    /**
     * Password for lol.fandom.com
     */
    private String lolFandomPassword;
}
