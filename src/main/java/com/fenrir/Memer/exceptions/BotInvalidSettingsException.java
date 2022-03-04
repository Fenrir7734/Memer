package com.fenrir.Memer.exceptions;

public class BotInvalidSettingsException extends Exception {
    public BotInvalidSettingsException(String message) {
        super(message);
    }

    public BotInvalidSettingsException(Throwable cause) {
        super(cause);
    }

    public BotInvalidSettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
