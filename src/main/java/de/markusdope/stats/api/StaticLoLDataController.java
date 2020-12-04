package de.markusdope.stats.api;

import com.merakianalytics.orianna.Orianna;
import de.markusdope.stats.service.VersionService;
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
import java.io.IOException;

@RestController
@RequestMapping("/static/{version}")
public class StaticLoLDataController {

    @Autowired
    private VersionService versionService;

    @GetMapping(value = "/champion/image/{championid}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getChampion(@PathVariable int championid, @PathVariable String version) {
        return this.imageToByteArray(
                Mono.just(championid)
                        .publishOn(Schedulers.boundedElastic())
                        .map(id -> Orianna.championWithId(id).withVersion(versionService.fromMatch(version)).get())
                        .map(champion -> champion.getImage().get())
        );
    }

    @GetMapping(value = "/summonerSpell/image/{spellId}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getSummonerSpell(@PathVariable int spellId, @PathVariable String version) {
        return this.imageToByteArray(
                Mono.just(spellId)
                        .publishOn(Schedulers.boundedElastic())
                        .map(id -> Orianna.summonerSpellWithId(id).withVersion(versionService.fromMatch(version)).get().getImage().get())
        );
    }

    @GetMapping(value = "/item/image/{itemId}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getItem(@PathVariable int itemId, @PathVariable String version) {
        return this.imageToByteArray(
                Mono.just(itemId)
                        .publishOn(Schedulers.boundedElastic())
                        .map(id -> Orianna.itemWithId(id).withVersion(versionService.fromMatch(version)).get().getImage().get())
        );
    }


    private Mono<byte[]> imageToByteArray(Mono<BufferedImage> imageMono) {
        return imageMono
                .flatMap(bufferedImage -> {
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        //While ImageIO.write can do IO operations, our usage operates only in memory, so doing this in an reactive context is ok
                        //noinspection BlockingMethodInNonBlockingContext
                        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
                        return Mono.just(byteArrayOutputStream.toByteArray());
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                });
    }
}
