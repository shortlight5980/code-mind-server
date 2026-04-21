package com.itsnow.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 消息表
 * @TableName messages
 */
@Data
public class MessagesVO {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 角色 (0-SYSTEM / 1-USER / 2-ASSISTANT / 3-TOOL)
     */
    private Integer role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createdAt;
}
