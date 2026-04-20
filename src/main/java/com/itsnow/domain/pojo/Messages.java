package com.itsnow.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 消息表
 * @TableName messages
 */
@Data
@TableName("messages")
public class Messages {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
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
