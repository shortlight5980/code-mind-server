package com.itsnow.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsnow.domain.pojo.Messages;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.pojo.Sessions;
import com.itsnow.domain.vo.MessagesVO;
import com.itsnow.exception.UnauthorizedException;
import com.itsnow.service.MessagesService;
import com.itsnow.mapper.MessagesMapper;
import com.itsnow.service.SessionsService;
import com.itsnow.utils.FormatConverter;
import com.itsnow.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.itsnow.constant.RedisConstants.MESSAGES_HISTORY_KEY;

/**
 * @author 14144
 * @description 针对表【messages(消息表)】的数据库操作Service实现
 * @createDate 2026-04-18 18:45:05
 */
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages>
        implements MessagesService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    @Lazy
    private SessionsService sessionsService;


    /**
     * 根据会话ID获取消息,用于上下文信息
     *
     * @param sessionId 会话ID
     * @return
     */
    @Override
    public List<Messages> getMessagesBySessionId(Long sessionId) {
        Sessions session = sessionsService.query().eq("id", sessionId).one();

        if (!Objects.equals(session.getUserId(), UserHolder.getUser().getId())) {
            // 未授权异常
            throw new UnauthorizedException();
        }

        // 查看redis中是否有记录
        List<JSONObject> history = getHistoryJsonList(sessionId);

        if (history != null) {
            List<Messages> result = new ArrayList<>();

            for (JSONObject messageJson : history) {
                Messages message = new Messages();
                String content = JSONUtil.toJsonStr(messageJson);
                message.setContent(content);
                message.setSessionId(sessionId);
                result.add(message);
            }

            return result;
        }

        LambdaQueryWrapper<Messages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Messages::getSessionId, sessionId)
                .orderByAsc(Messages::getCreatedAt);
        return list(queryWrapper);
    }

    /**
     * 获取历史消息，用于前端展示
     *
     * @param sessionId
     * @return
     */
    @Override
    public Result<List<MessagesVO>> getHistoryMessages(Long sessionId) {
        Sessions session = sessionsService.query().eq("id", sessionId).one();

        if (!Objects.equals(session.getUserId(), UserHolder.getUser().getId())) {
            return Result.error("无权限");
        }

        List<JSONObject> history = getHistoryJsonList(sessionId);
        if (history != null) {
            List<MessagesVO> result = new ArrayList<>();

            for (JSONObject messageJson : history) {
                MessagesVO message = new MessagesVO();
                String content = FormatConverter.processChunk(JSONUtil.toJsonStr(messageJson));
                message.setContent(content);
                String type = (String) messageJson.get("type");
                switch (type) {
                    case "human" -> message.setRole(1);
                    case "ai" -> message.setRole(2);
                    case "tool" -> message.setRole(3);
                    default -> message.setRole(0);
                }
                message.setSessionId(sessionId);
                message.setId(RandomUtil.randomLong(1, 10000000));
                message.setCreatedAt(new Date());
                result.add(message);
            }

            return Result.success(result);
        }


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

    private List<JSONObject> getHistoryJsonList(Long sessionId) {
        Sessions session = sessionsService.query().eq("id", sessionId).one();

        if (!Objects.equals(session.getUserId(), UserHolder.getUser().getId())) {
            // 未授权异常
            throw new UnauthorizedException();
        }

        // 先看redis中有没有记录
        String key = MESSAGES_HISTORY_KEY + sessionId;

        if (stringRedisTemplate.hasKey(key)) {
            Object historyObj = stringRedisTemplate.opsForHash()
                    .get(key, "history");
            List<JSONObject> history = new ArrayList<>();

            if (historyObj != null) {
                history = JSONUtil.parseArray(historyObj.toString()).toList(JSONObject.class);
            }

            Object messagesObj = stringRedisTemplate.opsForHash()
                    .get(key, "messages");
            List<JSONObject> messages = new ArrayList<>();
            if (messagesObj != null) {
                messages = JSONUtil.parseArray(messagesObj.toString()).toList(JSONObject.class);
            }

            history.addAll(messages);

            return history;
        }

        return null;
    }

}




