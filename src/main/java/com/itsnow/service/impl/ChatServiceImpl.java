package com.itsnow.service.impl;

import cloud.tianai.captcha.cache.CacheStore;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsnow.domain.pojo.Messages;
import com.itsnow.service.ChatService;
import com.itsnow.service.MessagesService;
import com.itsnow.service.SessionsService;
import com.itsnow.utils.FormatConverter;
import com.itsnow.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.listener.AbstractAuditListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.itsnow.constant.RedisConstants.*;

/**
 * 聊天服务实现
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private SessionsService sessionsService;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<String> chat(Map<String, Object> requestBody) {
        Object sessionIdObj = requestBody.get("sessionId");
        Long sessionId = sessionIdObj instanceof Number
                ? ((Number) sessionIdObj).longValue()
                : null;

        return Mono.deferContextual(ctx -> {
            Long userId = ctx.get("userId");
            requestBody.put("history", _getHistoryMessages(sessionId, userId));
            return webClient.post()
                    .uri("/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);
        });
    }

    @Override
    public Flux<String> chatStream(Map<String, Object> requestBody) {
        Object sessionIdObj = requestBody.get("sessionId");
        Long sessionId = sessionIdObj instanceof Number
                ? ((Number) sessionIdObj).longValue()
                : null;

        return Flux.deferContextual(ctx -> {
                    Long userId = ctx.get("userId");
                    requestBody.put("history", _getHistoryMessages(sessionId, userId));
                    log.info("requestBody: " + requestBody);

                    return webClient.post()
                            .uri("/chat/stream")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToFlux(String.class)
                            .filter(chunk -> !chunk.isEmpty())
                            .map(chunk -> {
                                if (chunk.startsWith("data: ")) {
                                    return chunk.substring(6);
                                }
                                return chunk;
                            })
                            .filter(chunk -> !"[DONE]".equals(chunk))
                            .concatMap(chunk -> saveMessage(chunk, sessionId, userId)
                                    .thenReturn(chunk)
                            )
                            .concatMap(chunk -> {
                                String processed = FormatConverter.processChunk(chunk);
                                return processed != null ? Mono.just(processed) : Mono.empty();
                            });
                })
                .doFinally(signalType -> {
                    if (signalType == SignalType.CANCEL) {
                        log.info("流被取消");
                    }
                });
    }

    /**
     * 检测服务是否正常
     *
     * @return
     */
    @Override
    public Mono<String> health() {
        return webClient.get()
                .uri("/health")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }


    /**
     * 获取历史消息（从UserHolder获取当前用户，用于非响应式上下文）
     */
    private List<Map<String, Object>> _getHistoryMessages(Long sessionId) {
        return _getHistoryMessages(sessionId, UserHolder.getUser().getId());
    }

    /**
     * 获取历史消息（显式传入用户ID，用于响应式上下文）
     *
     * @return 历史消息列表
     */
    private List<Map<String, Object>> _getHistoryMessages(Long sessionId, Long userId) {
        List<Messages> messages = messagesService.getMessagesBySessionId(sessionId, userId);
        List<Map<String, Object>> history = new ArrayList<>();

        for (Messages msg : messages) {
            JSONObject json = JSONUtil.parseObj(msg.getContent());
            Map<String, Object> messageMap = convertToStandardMap(json);
            history.add(messageMap);
        }
        return history;
    }

    private Map<String, Object> convertToStandardMap(JSONObject jsonObject) {
        Map<String, Object> result = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            result.put(key, convertToStandardType(value));
        }
        return result;
    }

    private Object convertToStandardType(Object value) {
        if (value instanceof JSONObject) {
            return convertToStandardMap((JSONObject) value);
        } else if (value instanceof cn.hutool.json.JSONArray) {
            cn.hutool.json.JSONArray jsonArray = (cn.hutool.json.JSONArray) value;
            List<Object> list = new ArrayList<>();
            for (Object item : jsonArray) {
                list.add(convertToStandardType(item));
            }
            return list;
        } else if (value instanceof cn.hutool.json.JSONNull) {
            return null;
        } else {
            return value;
        }
    }

    private Mono<Void> saveMessage(String content, Long sessionId, Long userId) {
        return Mono.fromCallable(() -> {
            // last_activate_time history messages [?isEnded]

            String key = MESSAGES_HISTORY_KEY + sessionId;

            // 更新session的updated_time
            if (stringRedisTemplate.getExpire(key) != -1L) {
                sessionsService.update()
                        .set("updated_time", LocalDateTime.now())
                        .eq("id", sessionId)
                        .update();
            }

            // 1.如果redis中没有记录
            if (!stringRedisTemplate.hasKey(key)) {
                // 获取历史记录
                List<Map<String, Object>> history = _getHistoryMessages(sessionId, userId);
                if (history != null && history.size() > 0) {
                    // 1.2 如果历史记录不为空
                    // 1.2.1 存储历史记录和message（message反正都要存储）
                    stringRedisTemplate.opsForHash()
                            .put(key, "history", JSONUtil.toJsonStr(history));
                }
                // 1.1 如果历史记录为空
                // 1.1.1 直接存储message
                JSONArray messagesArray = new JSONArray();
                JSONObject messageJson = JSONUtil.parseObj(content);
                messageJson.set("tmpId", System.currentTimeMillis() + "-" + Thread.currentThread().getId());
                messagesArray.add(messageJson);
                stringRedisTemplate.opsForHash()
                        .put(key, "messages", JSONUtil.toJsonStr(messagesArray));
            } else {
                // 2. 如果redis中有记录

                // 2.1 获取messages
                Object obj = stringRedisTemplate.opsForHash().get(key, "messages");
                JSONArray messagesArray;
                if (obj == null) {
                    messagesArray = new JSONArray();
                } else {
                    // 2.2 将新消息添加进去
                    messagesArray = JSONUtil.parseArray(obj.toString());
                }
                JSONObject messageJson = JSONUtil.parseObj(content);
                messageJson.set("tmpId", System.currentTimeMillis() + "-" + Thread.currentThread().getId());
                messagesArray.add(messageJson);
                // 2.3 存入messages
                stringRedisTemplate.opsForHash()
                        .put(key, "messages", JSONUtil.toJsonStr(messagesArray));
            }

            // 如果有过期时间
            Long expire = stringRedisTemplate.getExpire(key);
            if (expire >= 0L) {
                // 移除过期时间
                stringRedisTemplate.persist(key);
            }

            // 重置last_activate_time
            stringRedisTemplate.opsForHash()
                    .put(key, "last_activate_time", LocalDateTime.now().toString());

            // 在session:index中添加sessionId
            stringRedisTemplate.opsForSet().add(MESSAGES_HISTORY_INDEX_KEY, key);

            return null;
        }).then();
    }


}
