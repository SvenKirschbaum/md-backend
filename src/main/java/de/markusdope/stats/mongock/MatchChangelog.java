package de.markusdope.stats.mongock;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.core.OriannaObject;
import com.merakianalytics.orianna.types.data.match.Frame;
import com.merakianalytics.orianna.types.data.match.Match;
import de.markusdope.stats.data.document.MatchDocument;

import java.util.List;

@ChangeLog(order = "002")
public class MatchChangelog {
    @ChangeSet(order = "001", id = "changeMatch", author = "markusdope")
    public void changeMatch(MongockTemplate mongockTemplate) {
        List<Match> matchList = mongockTemplate.findAll(Match.class);
        for (Match match : matchList) {
            MatchDocument matchDocument = new MatchDocument();
            matchDocument.setId(match.getId());
            matchDocument.setMatch(match);
            matchDocument.setTimeline(Orianna.timelineWithId(match.getId()).get().stream().map(OriannaObject::getCoreData).toArray(Frame[]::new));
            mongockTemplate.save(matchDocument);
            mongockTemplate.remove(match);
        }
        mongockTemplate.dropCollection(Match.class);
    }
}
