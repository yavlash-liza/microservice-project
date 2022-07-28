package com.yavlash.api.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

import static java.time.ZonedDateTime.now;

@Data
@Accessors(chain = true)
public class Event<K, T> {
    private Type eventType;
    private K key;
    private T data;
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    private ZonedDateTime eventCreatedAt = now();

    public enum Type {
        CREATE,
        DELETE
    }
}