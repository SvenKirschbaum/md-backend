package de.markusdope.stats.api;

import com.merakianalytics.orianna.Orianna;
import de.markusdope.stats.exception.NotFoundException;
import de.markusdope.stats.service.VersionService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/static/{version}")
public class StaticLoLDataController {

    @Autowired
    private VersionService versionService;

    @GetMapping(value = "/champion/image/{championid}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getChampion(@PathVariable int championid, @PathVariable String version) {
        return Mono.just(championid)
            .publishOn(Schedulers.boundedElastic())
            .map(id -> Orianna.championWithId(id).withVersion(versionService.fromMatch(version)).get())
            .flatMap(champion -> champion.exists() ? Mono.just(champion) : Mono.error(new NotFoundException()))
            .map(champion -> champion.getImage().get())
            .map(this::imageToByteArray);
    }

    @GetMapping(value = "/summonerSpell/image/{spellId}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getSummonerSpell(@PathVariable int spellId, @PathVariable String version) {
        return Mono.just(spellId)
            .publishOn(Schedulers.boundedElastic())
            .map(id -> Orianna.summonerSpellWithId(id).withVersion(versionService.fromMatch(version)).get())
            .flatMap(spell -> spell.exists() ? Mono.just(spell) : Mono.error(new NotFoundException()))
            .map(spell -> spell.getImage().get())
            .map(this::imageToByteArray);
    }

    @GetMapping(value = "/item/image/{itemId}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getItem(@PathVariable int itemId, @PathVariable String version) {
        return Mono.just(itemId)
            .publishOn(Schedulers.boundedElastic())
            .map(id -> Orianna.itemWithId(id).withVersion(versionService.fromMatch(version)).get())
            .flatMap(item -> item.exists() ? Mono.just(item) : Mono.error(new NotFoundException()))
            .map(item -> item.getImage().get())
            .map(this::imageToByteArray);
    }


    //Our usage of ImageIO.write operates only in memory, so the IO Exception should not be able to occur
    @SneakyThrows
    private byte[] imageToByteArray(BufferedImage image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
