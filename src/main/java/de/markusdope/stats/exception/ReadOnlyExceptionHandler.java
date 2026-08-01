package de.markusdope.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReadOnlyExceptionHandler {
    @ExceptionHandler(ReadOnlyException.class)
    public ProblemDetail handleReadOnly() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, "Match data is read-only");
    }
}
