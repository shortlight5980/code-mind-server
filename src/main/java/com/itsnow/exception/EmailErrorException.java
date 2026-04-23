package com.itsnow.exception;

/**
 * @author itsnow
 * @date 2026/4/23
 */
public class EmailErrorException extends BaseException {
    public EmailErrorException() {
    }

    public EmailErrorException(String message) {
        super(message);
    }
}
