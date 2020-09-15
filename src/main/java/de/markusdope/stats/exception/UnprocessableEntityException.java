package de.markusdope.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Unprocessable Entity")
public class UnprocessableEntityException extends RuntimeException {
}
