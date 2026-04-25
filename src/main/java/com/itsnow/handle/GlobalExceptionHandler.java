package com.itsnow.handle;

import com.itsnow.constant.MessageConstant;
import com.itsnow.domain.pojo.Result;
import com.itsnow.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 * @author itsnow
 * @date 2026/4/22
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 用户名已存在异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String username = split[2];
            String mes = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(mes);
        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

    /**
     * 账号不存在
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(AccountNotFoundException ex){
        log.error(MessageConstant.ACCOUNT_NOT_FOUND);
        return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
    }

    /**
     * 密码错误
     */
    @ExceptionHandler
    public Result exceptionHandler(PasswordErrorException ex){
        log.error(MessageConstant.PASSWORD_ERROR);
        return Result.error(MessageConstant.PASSWORD_ERROR);
    }

    /**
     * 账号被冻结
     */
    @ExceptionHandler
    public Result exceptionHandler(AccountLockedException ex){
        log.error(MessageConstant.ACCOUNT_LOCKED);
        return Result.error(MessageConstant.ACCOUNT_LOCKED);
    }

    /**
     * 登录失败
     */
    @ExceptionHandler
    public Result exceptionHandler(LoginFailedException ex){
        log.error("错误信息：{}", ex.getMessage());
        return Result.error(MessageConstant.LOGIN_FAILED);
    }

    /**
     * 手机号格式错误
     */
    @ExceptionHandler
    public Result exceptionHandler(PhoneErrorException ex){
        log.error(MessageConstant.PHONE_ERROR);
        return Result.error(MessageConstant.PHONE_ERROR);
    }

    /**
     * 验证码错误
     */
    @ExceptionHandler
    public Result exceptionHandler(CodeErrorException ex){
        log.error(MessageConstant.CODE_ERROR);
        return Result.error(MessageConstant.CODE_ERROR);
    }

    /**
     * 邮箱格式错误
     */
    @ExceptionHandler
    public Result exceptionHandler(EmailErrorException ex){
        log.error(MessageConstant.EMAIL_ERROR);
        return Result.error(MessageConstant.EMAIL_ERROR);
    }

    /**
     * 图像验证码token无效
     */
    @ExceptionHandler
    public Result exceptionHandler(CaptchaTokenErrorException ex){
        log.error(MessageConstant.CAPTCHA_TOKEN_ERROR);
        return Result.error(MessageConstant.CAPTCHA_TOKEN_ERROR);
    }

    /**
     * 未授权
     */
    @ExceptionHandler
    public Result exceptionHandler(UnauthorizedException ex){
        log.error(MessageConstant.UNAUTHORIZED);
        return Result.error(MessageConstant.UNAUTHORIZED);
    }

}
