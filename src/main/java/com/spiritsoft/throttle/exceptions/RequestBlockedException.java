package com.spiritsoft.throttle.exceptions;

public class RequestBlockedException extends RuntimeException {

    public RequestBlockedException(String message) {
        super(message);
    }

}
