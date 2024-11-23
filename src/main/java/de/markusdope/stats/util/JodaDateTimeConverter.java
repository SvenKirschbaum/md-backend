package de.markusdope.stats.util;

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JodaDateTimeConverter implements Converter<Date, DateTime> {

    @Override
    public DateTime convert(Date source) {
        return new DateTime(source);
    }
}
