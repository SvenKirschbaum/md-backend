package de.markusdope.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_GATEWAY, reason = "Data Dragon request failed")
public class BadGatewayException extends RuntimeException {
    public BadGatewayException() {
    }

    public BadGatewayException(Throwable cause) {
        super(cause);
    }
}
