package com.fenrir.Memer.exceptions;

public class BotRuntimeException extends RuntimeException {
    public BotRuntimeException(String message) {
        super(message);
    }

    public BotRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
