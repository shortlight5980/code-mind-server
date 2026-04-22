package com.itsnow.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.itsnow.enums.SessionStatus;
import lombok.Data;

/**
 * 会话表
 * @TableName sessions
 */
@Data
@TableName("sessions")
public class Sessions {
    /**
     * 主键id
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联的仓库ID（当前工作上下文）
     */
    private Integer repoId;

    /**
     * 会话标题（可由第一条消息生成）
     */
    private String title;

    /**
     * 状态 (0-ACTIVE / 1-ARCHIVED / 2-DELETED)
     */
    private SessionStatus status;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 最后活动时间
     */
    private Date updatedTime;
}
