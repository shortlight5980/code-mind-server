package com.itsnow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author itsnow
 * @date 2026/4/22
 */
public enum LoginType {
    PHONE_CODE(0),
    ACCOUNT_PASSWORD(1),
    EMAIL_CODE(2);

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
