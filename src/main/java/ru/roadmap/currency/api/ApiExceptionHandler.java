package ru.roadmap.currency.api;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String,String> notFound(NoSuchElementException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(java.util.concurrent.RejectedExecutionException.class) @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    Map<String,String> rejected(RuntimeException e) { return Map.of("error", e.getMessage()); }
}
