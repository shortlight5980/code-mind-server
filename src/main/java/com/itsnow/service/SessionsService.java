package com.itsnow.service;

import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.pojo.Sessions;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itsnow.domain.vo.SessionsVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;

import java.net.http.HttpRequest;
import java.util.List;

/**
* @author 14144
* @description 针对表【sessions(会话表)】的数据库操作Service
* @createDate 2026-04-18 18:45:43
*/
public interface SessionsService extends IService<Sessions> {

    /**
     * 根据用户id查询会话历史
     * @param id
     * @return
     */
    Result<List<SessionsVO>> getByUserId(Long id);

    /**
     * 创建会话
     * @return
     */
    Result addSession();

    /**
     * 归档会话
     * @param id
     * @return
     */
    Result archiveSession(Long id);

    /**
     * 删除会话
     * @param id
     * @return
     */
    Result deleteSession(Long id);

    /**
     * 结束会话
     * @param sessionId
     * @return
     */
    Result endSession(Long sessionId);
}
