package de.markusdope.stats.util;

import org.bson.Document;
import org.joda.time.Duration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JodaDurationConverter implements Converter<Document, Duration> {
    @Override
    public Duration convert(Document source) {
        return Duration.millis(source.getLong("iMillis"));
    }
}
