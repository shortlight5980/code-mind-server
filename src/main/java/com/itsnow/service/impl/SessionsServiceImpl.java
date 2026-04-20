package com.itsnow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsnow.domain.pojo.Sessions;
import com.itsnow.service.SessionsService;
import com.itsnow.mapper.SessionsMapper;
import org.springframework.stereotype.Service;

/**
* @author 14144
* @description 针对表【sessions(会话表)】的数据库操作Service实现
* @createDate 2026-04-18 18:45:43
*/
@Service
public class SessionsServiceImpl extends ServiceImpl<SessionsMapper, Sessions>
    implements SessionsService{

}




