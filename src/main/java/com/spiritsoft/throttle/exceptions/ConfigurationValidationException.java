package com.spiritsoft.throttle.exceptions;

public class ConfigurationValidationException extends RuntimeException {
    public ConfigurationValidationException(String message) {
        super(message);
    }
    public ConfigurationValidationException(Throwable t) {
        super(t);
    }
}
