package com.itsnow.exception;

/**
 * @author itsnow
 * @date 2026/4/23
 */
public class CaptchaTokenErrorException extends BaseException {
    public CaptchaTokenErrorException() {}
    public CaptchaTokenErrorException(String message) {
        super(message);
    }
}
