package com.yavlash.util.http;

import com.yavlash.api.exception.BadRequestException;
import com.yavlash.api.exception.InvalidInputException;
import com.yavlash.api.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
class GlobalControllerExceptionHandler {
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public @ResponseBody HttpErrorInfo handleBadRequestExceptions(
            ServerHttpRequest request, BadRequestException ex) {
        return createHttpErrorInfo(BAD_REQUEST, request, ex);
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody HttpErrorInfo handleNotFoundExceptions(
            ServerHttpRequest request, NotFoundException e) {
        return createHttpErrorInfo(NOT_FOUND, request, e);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody
    HttpErrorInfo handleInvalidInputException(
            ServerHttpRequest request, InvalidInputException e) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, e);
    }

    private HttpErrorInfo createHttpErrorInfo(
            HttpStatus httpStatus, ServerHttpRequest request, Exception e) {
        final String path = request.getPath().pathWithinApplication().value();
        final String message = e.getMessage();
        log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
        return new HttpErrorInfo()
                .setHttpStatus(httpStatus)
                .setPath(path)
                .setMessage(message);
    }
}