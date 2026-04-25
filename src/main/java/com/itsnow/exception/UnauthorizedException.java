package com.itsnow.exception;

/**
 * @author itsnow
 * @date 2026/4/25
 */
public class UnauthorizedException extends BaseException {
    public UnauthorizedException() {}
    public UnauthorizedException(String message) {
        super(message);
    }
}
