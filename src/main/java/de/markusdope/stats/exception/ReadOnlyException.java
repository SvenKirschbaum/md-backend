package de.markusdope.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.GONE, reason = "Match data is read-only")
public class ReadOnlyException extends RuntimeException {
}
