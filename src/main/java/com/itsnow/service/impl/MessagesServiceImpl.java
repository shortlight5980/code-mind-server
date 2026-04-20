package com.itsnow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsnow.domain.pojo.Messages;
import com.itsnow.service.MessagesService;
import com.itsnow.mapper.MessagesMapper;
import org.springframework.stereotype.Service;

/**
* @author 14144
* @description 针对表【messages(消息表)】的数据库操作Service实现
* @createDate 2026-04-18 18:45:05
*/
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages>
    implements MessagesService{

}




