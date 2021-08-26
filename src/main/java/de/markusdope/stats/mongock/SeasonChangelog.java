package de.markusdope.stats.mongock;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import de.markusdope.stats.data.document.MatchDocument;
import lombok.Data;
import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

@ChangeLog(order = "003")
public class SeasonChangelog {
    @ChangeSet(order = "001", id = "addSeasonField", author = "markusdope")
    public void addSeasonField(MongockTemplate mongockTemplate) {
        List<MatchDocument> matches = mongockTemplate.findAll(MatchDocument.class);
        for (MatchDocument match : matches) {
            match.setSeason(1);

            mongockTemplate.save(match);
        }
    }

    @ChangeSet(order = "002", id = "addSeasonIndex", author = "markusdope")
    public void addSeasonIndex(MongoDatabase db) {
        MongoCollection<MatchDocument> matchDocumentCollection = db.getCollection("matchDocument", MatchDocument.class);
        matchDocumentCollection.createIndex(Indexes.ascending("season"));
    }
}
