package com.itsnow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itsnow.config.RSAProperties;
import com.itsnow.constant.MessageConstant;
import com.itsnow.domain.dto.LoginFormDTO;
import com.itsnow.domain.dto.RegistFormDTO;
import com.itsnow.domain.dto.UserDTO;
import com.itsnow.domain.pojo.Result;
import com.itsnow.domain.pojo.User;
import com.itsnow.domain.vo.UserVO;
import com.itsnow.enums.UserStatus;
import com.itsnow.exception.*;
import com.itsnow.service.UserService;
import com.itsnow.mapper.UserMapper;
import com.itsnow.utils.PasswordEncoder;
import com.itsnow.utils.RSAUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.itsnow.constant.RedisConstants.LOGIN_USER_KEY;
import static com.itsnow.constant.RedisConstants.LOGIN_USER_TTL;

/**
 * @author 14144
 * @description 针对表【users(用户表)】的数据库操作Service实现
 * @createDate 2026-04-22 12:45:40
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private RSAProperties rsaProperties;

    public UserServiceImpl(RSAProperties rsaProperties) {
        this.rsaProperties = rsaProperties;
    }

    /**
     * 登录
     *
     * @param loginForm
     * @return
     */
    @Override
    public Result<String> login(LoginFormDTO loginForm) {
        try {
            String token = switch (loginForm.getLoginType()) {
                case USERNAME_PASSWORD -> loginWithAccountPassword(loginForm);
                default -> null;
            };

            if (token != null) {
                return Result.success(token);
            }

        } catch (PasswordErrorException e) {
            throw new PasswordErrorException();

        } catch (Exception e) {
            throw new LoginFailedException(e.getMessage());

        }

        return Result.error(MessageConstant.LOGIN_FAILED);
    }

    private String loginWithAccountPassword(LoginFormDTO loginForm) throws Exception {
        // 取出加密后的密码使用私钥解密
        String password = loginForm.getPassword();
        password = RSAUtils.decrypt(password, RSAUtils.base64ToPrivateKey(rsaProperties.getPrivateKey()));

        // 根据用户名查询用户
        User user = this.getOne(new QueryWrapper<User>().eq("username", loginForm.getAccount()));

        // 账号被冻结
        if (user.getEnabled() == UserStatus.DISABLED) {
            throw new AccountLockedException();
        }

        // 用户不存在，登录失败
        if (user == null) {
            throw new AccountNotFoundException();
        }

        try {
            // 比对密码
            if (PasswordEncoder.check(password, user.getPassword())) {
                // 登录成功，保存登录信息
                // 保存用户信息到redis
                // 生成随机token
                String token = UUID.randomUUID().toString(true);

                // 将用户信息转为map
                UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
                Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions
                        .create().ignoreNullValue()
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

                // 保存用户信息
                stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
                // 设置过期时间
                stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
                return token;
            }
            throw new PasswordErrorException();
        } catch (Exception e) {
            throw new PasswordErrorException();
        }
    }

    /**
     *  注册
     * @param registFormDTO
     * @return
     * @throws Exception
     */
    public Result regist(RegistFormDTO registFormDTO) throws Exception {
        // 取出加密后的密码使用私钥解密
        String password = registFormDTO.getPassword();
        password = RSAUtils.decrypt(password, RSAUtils.base64ToPrivateKey(rsaProperties.getPrivateKey()));

        // 重新加密密码
        // 加密：内部自动生成随机盐 + 慢哈希
        String encodedPassword = PasswordEncoder.encrypt(password);
        User user = new User();
        user.setUsername(registFormDTO.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(registFormDTO.getEmail());
        user.setEnabled(UserStatus.ENABLED);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 存储用户信息
        this.save(user);

        return Result.success();
    }

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    @Override
    public Result<UserVO> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Long userId = Long.valueOf(stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "id").toString());
        User user = this.getById(userId);
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return Result.success(userVO);
    }
}




