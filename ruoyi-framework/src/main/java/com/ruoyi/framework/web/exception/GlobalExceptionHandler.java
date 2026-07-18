package com.ruoyi.framework.web.exception;

import java.sql.SQLException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.DemoModeException;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.BlackListException;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.exception.user.UserNotExistsException;
import com.ruoyi.common.exception.user.UserPasswordExpireException;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.exception.user.UserPasswordRetryLimitExceedException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.EscapeUtil;

/**
 * 全局异常处理器
 * 
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 权限校验异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
            HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request)
    {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String value = Convert.toStr(e.getValue());
        if (StringUtils.isNotEmpty(value))
        {
            value = EscapeUtil.clean(value);
        }
        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), value));
    }

    /**
     * 空指针异常 → 业务化提示（不暴露技术细节）
     */
    @ExceptionHandler(NullPointerException.class)
    public AjaxResult handleNullPointerException(NullPointerException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生空指针异常.", requestURI, e);
        return AjaxResult.error("系统处理异常，请联系管理员");
    }

    /**
     * SQL异常 → 业务化提示（不暴露技术细节）
     */
    @ExceptionHandler(SQLException.class)
    public AjaxResult handleSQLException(SQLException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生SQL异常.", requestURI, e);
        return AjaxResult.error("数据操作异常，请稍后重试");
    }

    /**
     * 参数校验异常 → 业务化提示（不暴露技术细节）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public AjaxResult handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生参数异常.", requestURI, e);
        return AjaxResult.error("请求参数有误，请检查后重试");
    }

    /**
     * 拦截未知的运行时异常 → 业务化提示（不暴露技术细节）
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return AjaxResult.error("系统繁忙，请稍后重试");
    }

    /**
     * 系统异常 → 业务化提示（不暴露技术细节）
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error("系统处理异常，请联系管理员");
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 演示模式异常
     */
    @ExceptionHandler(DemoModeException.class)
    public AjaxResult handleDemoModeException(DemoModeException e)
    {
        return AjaxResult.error("演示模式，不允许操作");
    }

    /**
     * 验证码过期异常
     */
    @ExceptionHandler(CaptchaExpireException.class)
    public AjaxResult handleCaptchaExpireException(CaptchaExpireException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',验证码已过期.", requestURI, e);
        return AjaxResult.error("验证码已过期，请重新获取");
    }

    /**
     * 验证码错误异常
     */
    @ExceptionHandler(CaptchaException.class)
    public AjaxResult handleCaptchaException(CaptchaException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',验证码错误.", requestURI, e);
        return AjaxResult.error("验证码错误，请重新输入");
    }

    /**
     * 用户不存在异常
     */
    @ExceptionHandler(UserNotExistsException.class)
    public AjaxResult handleUserNotExistsException(UserNotExistsException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',用户不存在.", requestURI, e);
        return AjaxResult.error("用户不存在，请检查用户名");
    }

    /**
     * 用户密码不匹配异常
     */
    @ExceptionHandler(UserPasswordNotMatchException.class)
    public AjaxResult handleUserPasswordNotMatchException(UserPasswordNotMatchException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',密码错误.", requestURI, e);
        return AjaxResult.error("密码错误，请重新输入");
    }

    /**
     * 用户密码过期异常
     */
    @ExceptionHandler(UserPasswordExpireException.class)
    public AjaxResult handleUserPasswordExpireException(UserPasswordExpireException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',用户密码已过期.", requestURI, e);
        return AjaxResult.error("密码已过期，请联系管理员重置");
    }

    /**
     * 登录重试次数超过限制异常
     */
    @ExceptionHandler(UserPasswordRetryLimitExceedException.class)
    public AjaxResult handleUserPasswordRetryLimitExceedException(UserPasswordRetryLimitExceedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',登录重试次数超过限制.", requestURI, e);
        return AjaxResult.error("登录失败次数过多，账户已锁定，请稍后重试");
    }

    /**
     * 黑名单异常
     */
    @ExceptionHandler(BlackListException.class)
    public AjaxResult handleBlackListException(BlackListException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',IP被加入黑名单.", requestURI, e);
        return AjaxResult.error("您的IP已被限制登录，请联系管理员");
    }
}
