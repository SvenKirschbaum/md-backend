package de.markusdope.stats.service;

import com.merakianalytics.orianna.Orianna;
import org.springframework.stereotype.Service;

@Service
public class VersionService {

    public String fromMatch(String version) {
        if ("latest".equals(version)) {
            return Orianna.getVersions().get(0);
        }
        return Orianna.getVersions().getBestMatch(version);
    }
}
