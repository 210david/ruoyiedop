package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 二次密码验证注解
 * 标记在需要二次密码验证的方法上，AOP切面会拦截并验证用户密码
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VerifyPassword
{
    /**
     * 请求参数中密码字段名，默认为 verifyPassword
     */
    public String paramName() default "verifyPassword";
}
