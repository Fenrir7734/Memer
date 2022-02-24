package com.fenrir.Memer.exceptions;

public class DatabaseException extends Exception {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        this(message, null);
    }
}