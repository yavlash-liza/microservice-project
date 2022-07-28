package com.yavlash.util.http;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class HttpErrorInfo {
    private ZonedDateTime timestamp = ZonedDateTime.now();
    private String path;
    private HttpStatus httpStatus;
    private String message;
}