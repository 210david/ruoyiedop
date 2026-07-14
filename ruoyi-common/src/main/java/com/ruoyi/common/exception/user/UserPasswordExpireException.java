package com.ruoyi.common.exception.user;

/**
 * 用户密码过期异常类
 * 
 * @author ruoyi
 */
public class UserPasswordExpireException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserPasswordExpireException()
    {
        super("user.password.expire", null);
    }
}
