package com.itsnow.service.impl;

import com.itsnow.domain.pojo.Messages;
import com.itsnow.domain.pojo.Sessions;
import com.itsnow.service.ChatService;
import com.itsnow.service.MessagesService;
import com.itsnow.service.SessionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;

/**
 * 聊天服务实现
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private SessionsService sessionsService;

    @Autowired
    private MessagesService messagesService;

    private static final Long DEFAULT_SESSION_ID = 1L;
    private static final Long DEFAULT_USER_ID = 1L;
    private static final Integer DEFAULT_REPO_ID = 1;

    @Override
    public Mono<String> chat(Map<String, Object> requestBody) {
        String question = (String) requestBody.get("question");

        return ensureSessionExists()
                .then(saveUserMessage(question))
                .then(webClient.post()
                        .uri("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(response -> {
                                    System.out.println(response);
                                    System.out.println("=================");
                                    return saveAssistantMessage(response)
                                            .thenReturn(response);
                                }

                        )
                );
    }

    @Override
    public Flux<String> chatStream(Map<String, Object> requestBody) {
        String question = (String) requestBody.get("question");

        return ensureSessionExists()
                .thenMany(saveUserMessage(question))
                .thenMany(webClient.post()
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
                        .concatMap(chunk -> saveAssistantMessage(chunk)
                                .thenReturn(chunk))
                );
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

    private Mono<Void> ensureSessionExists() {
        return Mono.fromCallable(() -> sessionsService.getById(DEFAULT_SESSION_ID))
                .flatMap(session -> {
                    if (session == null) {
                        Sessions newSession = new Sessions();
                        newSession.setId(DEFAULT_SESSION_ID);
                        newSession.setUserId(DEFAULT_USER_ID);
                        newSession.setRepoId(DEFAULT_REPO_ID);
                        newSession.setTitle("默认会话");
                        newSession.setStatus(0);
                        newSession.setCreatedTime(new Date());
                        newSession.setUpdatedTime(new Date());
                        return Mono.fromCallable(() -> {
                            sessionsService.save(newSession);
                            return null;
                        });
                    }
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> saveUserMessage(String content) {
        return Mono.fromCallable(() -> {
            Messages message = new Messages();
            message.setSessionId(DEFAULT_SESSION_ID);
            message.setRole(1);
            message.setContent(content);
            message.setCreatedAt(new Date());
            messagesService.save(message);
            return null;
        }).then();
    }

    private Mono<Void> saveAssistantMessage(String content) {
        return Mono.fromCallable(() -> {
            Messages message = new Messages();
            message.setSessionId(DEFAULT_SESSION_ID);
            message.setRole(2);
            message.setContent(content);
            message.setCreatedAt(new Date());
            messagesService.save(message);
            return null;
        }).then();
    }
}
