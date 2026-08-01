package de.markusdope.stats.mongock;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.core.OriannaObject;
import de.markusdope.stats.data.document.MatchDocument;
import de.markusdope.stats.data.match.Event;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.List;

@ChangeLog(order = "002")
public class MatchChangelog {
    private static final JsonMapper MAPPER = JsonMapper.builder().addModule(new JodaModule()).build();

    @ChangeSet(order = "001", id = "changeMatch", author = "markusdope")
    public void changeMatch(MongockTemplate mongockTemplate) {
        List<com.merakianalytics.orianna.types.data.match.Match> matchList =
                mongockTemplate.findAll(com.merakianalytics.orianna.types.data.match.Match.class);
        for (com.merakianalytics.orianna.types.data.match.Match match : matchList) {
            MatchDocument matchDocument = new MatchDocument();
            matchDocument.setId(match.getId());
            matchDocument.setMatch(MAPPER.convertValue(match, de.markusdope.stats.data.match.Match.class));
            matchDocument.setTimeline(Orianna.timelineWithId(match.getId()).get().stream()
                    .map(OriannaObject::getCoreData)
                    .map(frame -> MAPPER.convertValue(frame, new TypeReference<List<Event>>() {}))
                    .toList());
            mongockTemplate.save(matchDocument);
            mongockTemplate.remove(match);
        }
        mongockTemplate.dropCollection(com.merakianalytics.orianna.types.data.match.Match.class);
    }
}
