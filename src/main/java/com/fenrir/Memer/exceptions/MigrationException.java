package com.fenrir.Memer.exceptions;

public class MigrationException extends Exception {
    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationException(String message) {
        this(message, null);
    }
}
