package com.itsnow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsnow.domain.pojo.Messages;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.vo.MessagesVO;
import com.itsnow.service.MessagesService;
import com.itsnow.mapper.MessagesMapper;
import com.itsnow.utils.FormatConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author 14144
* @description 针对表【messages(消息表)】的数据库操作Service实现
* @createDate 2026-04-18 18:45:05
*/
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages>
    implements MessagesService{

    /**
     * 根据会话ID获取消息,用于上下文信息
     * @param sessionId 会话ID
     * @return
     */
    @Override
    public List<Messages> getMessagesBySessionId(Long sessionId) {
        LambdaQueryWrapper<Messages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Messages::getSessionId, sessionId)
                .orderByAsc(Messages::getCreatedAt);
        return list(queryWrapper);
    }

    /**
     * 获取历史消息，用于前端展示
     * @param sessionId
     * @return
     */
    @Override
    public Result getHistoryMessages(Long sessionId) {
        LambdaQueryWrapper<Messages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Messages::getSessionId, sessionId)
                .orderByAsc(Messages::getCreatedAt);

        List<Messages> messagesList = this.list(queryWrapper);

        List<MessagesVO> voList = messagesList.stream().map(message -> {
            MessagesVO vo = new MessagesVO();
            BeanUtils.copyProperties(message, vo);
            String content = FormatConverter.processChunk(message.getContent());
            vo.setContent(content);
            return vo;
        }).collect(Collectors.toList());

        // 4. 返回成功结果
        return Result.success(voList);
    }

}




