package com.itsnow.service;

import com.itsnow.domain.pojo.Messages;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.vo.MessagesVO;

import java.util.List;

/**
* @author 14144
* @description 针对表【messages(消息表)】的数据库操作Service
* @createDate 2026-04-18 18:45:05
*/
public interface MessagesService extends IService<Messages> {

    /**
     * 根据会话ID查询历史消息,用于上下文信息
     * @param sessionId 会话ID
     * @return 历史消息列表，按创建时间升序排列
     */
    List<Messages> getMessagesBySessionId(Long sessionId);

    /**
     * 获取历史消息，用于前端展示
     * @param sessionId
     * @return
     */
    Result<List<MessagesVO>> getHistoryMessages(Long sessionId);

}
