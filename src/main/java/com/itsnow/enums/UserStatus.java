package com.itsnow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author itsnow
 * @date 2026/4/22
 */
public enum UserStatus {
    ENABLED(1),
    DISABLED(0);

    @JsonValue
    @EnumValue
    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
