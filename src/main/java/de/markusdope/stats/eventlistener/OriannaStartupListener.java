package de.markusdope.stats.eventlistener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Platform;
import com.merakianalytics.orianna.types.common.Region;
import de.markusdope.stats.config.MarkusDopeStatsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OriannaStartupListener implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private MarkusDopeStatsProperties properties;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Orianna.setDefaultPlatform(Platform.EUROPE_WEST);
        Orianna.setDefaultRegion(Region.EUROPE_WEST);
        Orianna.setDefaultLocale("de_DE");
        Orianna.setRiotAPIKey(properties.getRiotApiKey());
    }
}
