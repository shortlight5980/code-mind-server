package com.itsnow.exception;

/**
 * @author itsnow
 * @date 2026/4/22
 */
public class BaseException extends RuntimeException {
    public BaseException() {}
    public BaseException(String message) {
        super(message);
    }
}
