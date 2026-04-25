package com.itsnow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 消息角色枚举
 */
public enum MessageRole {
    SYSTEM(0),
    USER(1),
    ASSISTANT(2),
    TOOL(3);

    @EnumValue
    @JsonValue
    private final int value;

    MessageRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageRole fromType(String type) {
        if (type == null) {
            return SYSTEM;
        }
        return switch (type) {
            case "human" -> USER;
            case "ai" -> ASSISTANT;
            case "tool" -> TOOL;
            default -> SYSTEM;
        };
    }
}
