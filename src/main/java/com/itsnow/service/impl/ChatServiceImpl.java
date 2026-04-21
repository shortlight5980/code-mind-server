package com.itsnow.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsnow.domain.pojo.Messages;
import com.itsnow.service.ChatService;
import com.itsnow.service.MessagesService;
import com.itsnow.service.SessionsService;
import com.itsnow.utils.FormatConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

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

    private static final Long DEFAULT_SESSION_ID = 1L;

    @Override
    public Mono<String> chat(Map<String, Object> requestBody) {
        Object sessionIdObj = requestBody.get("sessionId");
        Long sessionId = sessionIdObj instanceof Number
                ? ((Number) sessionIdObj).longValue()
                : null;

        return sessionsService.ensureSessionExists()
                .then(Mono.fromCallable(() -> {
                    requestBody.put("history", _getHistoryMessages(sessionId));
                    return requestBody;
                }))
                .flatMap(body -> webClient.post()
                        .uri("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                );
    }

    @Override
    @Transactional
    public Flux<String> chatStream(Map<String, Object> requestBody) {
        Object sessionIdObj = requestBody.get("sessionId");
        Long sessionId = sessionIdObj instanceof Number
                ? ((Number) sessionIdObj).longValue()
                : null;

        return sessionsService.ensureSessionExists()
                .thenMany(Mono.fromCallable(() -> {
                            requestBody.put("history", _getHistoryMessages(sessionId));
                            log.info("requestBody: " + requestBody);
                            return requestBody;
                        })
                        .flatMapMany(body -> webClient.post()
                                .uri("/chat/stream")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.TEXT_EVENT_STREAM)
                                .bodyValue(body)
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
                                .concatMap(chunk -> {
                                            sessionsService.update()
                                                    .set("updated_time", LocalDateTime.now())
                                                    .eq("id", sessionId);
                                            return saveMessage(chunk, sessionId)
                                                    .thenReturn(chunk);
                                        }
                                )
                                .concatMap(chunk -> {
                                    String processed = FormatConverter.processChunk(chunk);
                                    return processed != null ? Mono.just(processed) : Mono.empty();
                                })

                        ));
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
     * 获取历史消息
     *
     * @return 历史消息列表
     */
    private List<Map<String, Object>> _getHistoryMessages(Long sessionId) {
        List<Messages> messages = messagesService.getMessagesBySessionId(sessionId);
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

    private Mono<Void> saveMessage(String content, Long sessionId) {
        return Mono.fromCallable(() -> {
            Messages message = new Messages();

            JSONObject json = JSONUtil.parseObj(content);
            switch ((String) json.get("type")) {
                case "human" -> message.setRole(1);
                case "ai" -> message.setRole(2);
                case "tool" -> message.setRole(3);
            }

            message.setSessionId(sessionId);
            message.setContent(content);
            message.setCreatedAt(new Date());
            messagesService.save(message);
            return null;
        }).then();
    }


}
