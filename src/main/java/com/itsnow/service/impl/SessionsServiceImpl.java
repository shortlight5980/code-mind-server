package com.itsnow.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsnow.domain.pojo.Messages;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.pojo.Sessions;
import com.itsnow.domain.vo.SessionsVO;
import com.itsnow.enums.SessionStatus;
import com.itsnow.exception.BaseException;
import com.itsnow.service.MessagesService;
import com.itsnow.service.SessionsService;
import com.itsnow.mapper.SessionsMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.itsnow.constant.RedisConstants.*;

/**
 * @author 14144
 * @description 针对表【sessions(会话表)】的数据库操作Service实现
 * @createDate 2026-04-18 18:45:43
 */
@Service
public class SessionsServiceImpl extends ServiceImpl<SessionsMapper, Sessions>
        implements SessionsService {

    private static final Integer DEFAULT_REPO_ID = 1;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    MessagesService messagesService;

    @Override
    public Result<List<SessionsVO>> getByUserId(Long id) {
        LambdaQueryWrapper<Sessions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sessions::getUserId, id)
                .eq(Sessions::getStatus, SessionStatus.ACTIVE)
                .orderByDesc(Sessions::getUpdatedTime);

        List<Sessions> sessionsList = this.list(queryWrapper);

        List<SessionsVO> voList = sessionsList.stream().map(session -> {
            SessionsVO vo = new SessionsVO();
            BeanUtils.copyProperties(session, vo);
            return vo;
        }).collect(Collectors.toList());

        // 4. 返回成功结果
        return Result.success(voList);
    }

    /**
     * 添加会话
     *
     * @return
     */
    @Override
    public Result addSession(HttpServletRequest request) {
        Sessions session = new Sessions();
        String token = request.getHeader("Authorization");
        Long userId = Long.valueOf(stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "id").toString());
        session.setUserId(userId);
        session.setRepoId(DEFAULT_REPO_ID);
        session.setTitle("新会话");
        session.setStatus(SessionStatus.ACTIVE);
        session.setCreatedTime(new Date());
        session.setUpdatedTime(new Date());
        this.save(session);
        return Result.success();
    }

    /**
     * 归档会话
     *
     * @param id
     * @return
     */
    @Override
    public Result archiveSession(Long id) {
        this.update()
                .set("status", SessionStatus.ARCHIVED)
                .eq("id", id)
                .update();
        return Result.success();
    }

    /**
     * 逻辑删除会话
     *
     * @param id
     * @return
     */
    @Override
    public Result deleteSession(Long id) {
        this.update()
                .set("status", SessionStatus.DELETED)
                .eq("id", id)
                .update();
        return Result.success();
    }

    /**
     * 结束会话
     *
     * @param sessionId
     * @return
     */
    @Override
    public Result endSession(Long sessionId) {

        String key = MESSAGES_HISTORY_KEY + sessionId;

        // 存储消息

        // 从Redis中获取会话信息
        Object obj = stringRedisTemplate.opsForHash()
                .get(key, "messages");

        if (obj == null) {
            return Result.success();
        }

        String messagesStr = obj.toString();

        try {
            // 使用Hutool解析JSON数组
            List<JSONObject> messageJsonList = JSONUtil.parseArray(messagesStr).toList(JSONObject.class);

            List<Messages> messages = new ArrayList<>();

            for (JSONObject json : messageJsonList) {
                Messages message = new Messages();

                // 根据type字段设置role
                String type = json.getStr("type");
                switch (type) {
                    case "human" -> message.setRole(1);
                    case "ai" -> message.setRole(2);
                    case "tool" -> message.setRole(3);
                    default -> message.setRole(0);
                }

                message.setSessionId(sessionId);
                message.setContent(json.toString());
                message.setCreatedAt(new Date());

                messages.add(message);
            }

            // 批量保存到数据库
            if (!messages.isEmpty()) {
                messagesService.saveBatch(messages);
            }

        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }

        // 更新history和messages
        Object o = stringRedisTemplate.opsForHash().get(key, "history");
        List<JSONObject> history = new ArrayList<>();
        if (o != null){
            String historyStr = o.toString();
            history = JSONUtil.parseArray(historyStr).toList(JSONObject.class);
        }

        List<JSONObject> newMessages = JSONUtil.parseArray(messagesStr).toList(JSONObject.class);

        history.addAll(newMessages);

        stringRedisTemplate.opsForHash().put(key, "history", JSONUtil.toJsonStr(history));
        stringRedisTemplate.opsForHash().delete(key, "messages");


//        // 设置isEnded为true
//        stringRedisTemplate.opsForHash().put(key, "isEnded", true);

        // 设置过期时间
        stringRedisTemplate.expire(key, MESSAGES_HISTORY_TTL, TimeUnit.MINUTES);

        // 移除session:index中的key
        stringRedisTemplate.opsForSet().remove(MESSAGES_HISTORY_INDEX_KEY, key);

        return Result.success();

    }
}




