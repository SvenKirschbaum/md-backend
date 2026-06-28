package de.markusdope.stats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.datatype.joda.JodaModule;

@Configuration
public class JacksonConfig {
    @Bean
    public JacksonModule jodaModule() {
        return new JodaModule();
    }
}
