package com.itsnow.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author itsnow
 * @date 2026/4/21
 */
@Data
public class SessionsVO {
    /**
     * 主键id
     */
    private Long id;

//    /**
//     * 关联的仓库ID（当前工作上下文）
//     */
//    private Integer repoId;

    /**
     * 会话标题（可由第一条消息生成）
     */
    private String title;

    /**
     * 最后活动时间
     */
    private Date updatedTime;
}
