package com.itsnow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author itsnow
 * @date 2026/4/21
 */
public enum SessionStatus {
    ACTIVE(0),
    ARCHIVED(1),
    DELETED(2);

    @EnumValue
    @JsonValue
    private final int value;

    SessionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
