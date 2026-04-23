package com.itsnow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
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
import com.itsnow.utils.RegexUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.itsnow.constant.RedisConstants.*;

/**
 * @author 14144
 * @description 针对表【users(用户表)】的数据库操作Service实现
 * @createDate 2026-04-22 12:45:40
 */
@Service
@Slf4j
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
    public Result<String> login(LoginFormDTO loginForm) throws Exception {
        String token = switch (loginForm.getLoginType()) {
            case ACCOUNT_PASSWORD -> loginWithAccountPassword(loginForm);
            case PHONE_CODE -> loginWithPhoneCode(loginForm);
            case EMAIL_CODE -> loginWithEmailCode(loginForm);
        };

        if (token != null) {
            return Result.success(token);
        }

        return Result.error(MessageConstant.LOGIN_FAILED);
    }

    private String loginWithEmailCode(LoginFormDTO loginForm) {
        // 校验邮箱
        String email = loginForm.getAccount();
        if (RegexUtils.isEmailInvalid(email)) {
            // 如果不正确，返回错误信息
            throw new EmailErrorException();
        }

        // 校验验证码
        String code = loginForm.getCode();
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + email);
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 异常则返回失败信息
            throw new CodeErrorException();
        }

        // 根据邮箱查询用户
        User user = query().eq("email", email).one();

        // 不存在
        if (user == null) {
            throw new AccountNotFoundException();
        }

        // 已冻结
        if (user.getEnabled() == UserStatus.DISABLED) {
            throw new AccountLockedException();
        }

        String token = this.createToken(user);

        return token;
    }

    private String loginWithPhoneCode(LoginFormDTO loginForm) {
        // 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 如果不正确，返回错误信息
            throw new PhoneErrorException();
        }

        // 校验验证码
        String code = loginForm.getCode();
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 异常则返回失败信息
            throw new CodeErrorException();
        }

        // 根据手机号查询用户
        User user = query().eq("phone", phone).one();

        // 不存在
        if (user == null) {
            throw new AccountNotFoundException();
        }

        // 已冻结
        if (user.getEnabled() == UserStatus.DISABLED) {
            throw new AccountLockedException();
        }

        String token = this.createToken(user);

        return token;
    }

    private String loginWithAccountPassword(LoginFormDTO loginForm) throws Exception {
        // 取出加密后的密码使用私钥解密
        String password = loginForm.getPassword();
        password = RSAUtils.decrypt(password, RSAUtils.base64ToPrivateKey(rsaProperties.getPrivateKey()));

        // 根据用户名、邮箱或手机号查询用户
        User user = this.getOne(
                new QueryWrapper<User>()
                        .eq("username", loginForm.getAccount())
                        .or()
                        .eq("email", loginForm.getAccount())
                        .or()
                        .eq("phone", loginForm.getAccount())
        );

        // 用户不存在，登录失败
        if (user == null) {
            throw new AccountNotFoundException();
        }

        // 账号被冻结
        if (user.getEnabled() == UserStatus.DISABLED) {
            throw new AccountLockedException();
        }

        try {
            // 比对密码
            if (PasswordEncoder.check(password, user.getPassword())) {
                // 登录成功，保存登录信息
                String token = this.createToken(user);
                return token;
            }
            throw new PasswordErrorException();
        } catch (Exception e) {
            throw new PasswordErrorException();
        }
    }

    /**
     * 创建token
     *
     * @param user
     * @return
     */
    private String createToken(User user) {
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

    /**
     * 注册
     *
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
     *
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

    /**
     * 发送验证码
     *
     * @param phone
     * @return
     */
    @Override
    public Result sendCode(String phone) {
        // 校验手机号格式是否正确
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 如果不正确，返回错误信息
            return Result.error("手机号码格式错误");
        }

        // 生成随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 将验证码保存到redis
//        session.setAttribute("code", code);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 发送验证码
        log.info("验证码为：{}", code);

        // 返回ok
        return Result.success();
    }
}




