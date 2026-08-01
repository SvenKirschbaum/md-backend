package de.markusdope.stats.api;

import de.markusdope.stats.service.DataDragonService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/static/{version}")
public class StaticLoLDataController {

    private final DataDragonService dataDragonService;

    public StaticLoLDataController(DataDragonService dataDragonService) {
        this.dataDragonService = dataDragonService;
    }

    @GetMapping(value = "/champion/image/{championid}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getChampion(@PathVariable int championid, @PathVariable String version) {
        return dataDragonService.championImage(championid, version);
    }

    @GetMapping(value = "/summonerSpell/image/{spellId}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getSummonerSpell(@PathVariable int spellId, @PathVariable String version) {
        return dataDragonService.summonerSpellImage(spellId, version);
    }

    @GetMapping(value = "/item/image/{itemId}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getItem(@PathVariable int itemId, @PathVariable String version) {
        return dataDragonService.itemImage(itemId, version);
    }
}
