package com.itsnow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.pojo.Sessions;
import com.itsnow.domain.vo.SessionsVO;
import com.itsnow.enums.SessionStatus;
import com.itsnow.service.SessionsService;
import com.itsnow.mapper.SessionsMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.itsnow.constant.RedisConstants.LOGIN_USER_KEY;

/**
* @author 14144
* @description 针对表【sessions(会话表)】的数据库操作Service实现
* @createDate 2026-04-18 18:45:43
*/
@Service
public class SessionsServiceImpl extends ServiceImpl<SessionsMapper, Sessions>
    implements SessionsService{

    private static final Long DEFAULT_SESSION_ID = 1L;
    private static final Integer DEFAULT_REPO_ID = 1;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

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
}




