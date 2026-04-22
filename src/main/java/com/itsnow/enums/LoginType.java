package com.itsnow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author itsnow
 * @date 2026/4/22
 */
public enum LoginType {
    MOBILE_CODE(0),
    USERNAME_PASSWORD(1),
    MOBILE_PASSWORD(2),
    EMAIL_CODE(3),
    EMAIL_PASSWORD(4);

    @EnumValue
    @JsonValue
    private final int value;

    LoginType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
