package com.itsnow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.pojo.Sessions;
import com.itsnow.domain.vo.SessionsVO;
import com.itsnow.service.SessionsService;
import com.itsnow.mapper.SessionsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 14144
* @description 针对表【sessions(会话表)】的数据库操作Service实现
* @createDate 2026-04-18 18:45:43
*/
@Service
public class SessionsServiceImpl extends ServiceImpl<SessionsMapper, Sessions>
    implements SessionsService{

    private static final Long DEFAULT_SESSION_ID = 1L;
    private static final Long DEFAULT_USER_ID = 1L;
    private static final Integer DEFAULT_REPO_ID = 1;

    @Override
    public Result getByUserId(Long id) {
        LambdaQueryWrapper<Sessions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sessions::getUserId, id)
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


    public Mono<Void> ensureSessionExists() {
        return Mono.fromCallable(() -> this.getById(DEFAULT_SESSION_ID))
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
                            this.save(newSession);
                            return null;
                        });
                    }
                    return Mono.empty();
                })
                .then();
    }

    /**
     * 添加会话
     * @return
     */
    @Override
    public Result addSession() {
        Sessions session = new Sessions();
        session.setUserId(DEFAULT_USER_ID);
        session.setRepoId(DEFAULT_REPO_ID);
        session.setTitle("新会话");
        session.setStatus(0);
        session.setCreatedTime(new Date());
        session.setUpdatedTime(new Date());
        this.save(session);
        return Result.success();
    }
}




