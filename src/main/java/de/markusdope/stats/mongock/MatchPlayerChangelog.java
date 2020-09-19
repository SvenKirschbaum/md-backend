package de.markusdope.stats.mongock;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

@ChangeLog(order = "001")
public class MatchPlayerChangelog {
    @ChangeSet(order = "001", id = "changeMatchPlayer", author = "markusdope")
    public void changeMatchPlayer(MongockTemplate mongockTemplate) {
        List<MatchPlayer> matchPlayerList = mongockTemplate.findAll(MatchPlayer.class);
        for (MatchPlayer omp : matchPlayerList) {
            de.markusdope.stats.data.document.MatchPlayer matchPlayer = new de.markusdope.stats.data.document.MatchPlayer();
            matchPlayer.setId(omp.getId());
            de.markusdope.stats.data.document.MatchPlayer.Player[] players = new de.markusdope.stats.data.document.MatchPlayer.Player[omp.getPlayers().size()];
            int i = 0;
            for (Map.Entry<Integer, String> entry : omp.getPlayers().entrySet()) {
                players[i] = new de.markusdope.stats.data.document.MatchPlayer.Player(entry.getKey(), entry.getValue());
                i++;
            }
            matchPlayer.setPlayers(players);
            mongockTemplate.save(matchPlayer);
        }
    }

    @Data
    private static class MatchPlayer {
        Map<Integer, String> players;
        @Id
        private long id;
    }
}
