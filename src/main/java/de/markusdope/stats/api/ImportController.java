package de.markusdope.stats.api;

import de.markusdope.stats.data.dto.ImportRequestDTO;
import de.markusdope.stats.exception.ReadOnlyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/import")
@PreAuthorize("hasRole('manager')")
public class ImportController {
    @GetMapping("/{matchId}")
    public Mono<Void> getAction(@PathVariable Long matchId) {
        return Mono.error(new ReadOnlyException());
    }

    @PostMapping("/{matchId}")
    public Mono<Void> postAction(@PathVariable Long matchId, @RequestBody ImportRequestDTO request) {
        return Mono.error(new ReadOnlyException());
    }
}
