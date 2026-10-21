package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据变更日志注解，用于记录实体数据的增删改操作
 * 
 * @author ruoyi
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataChangeLog
{
    /**
     * 实体名称（如：用户管理、角色管理）
     */
    public String value() default "";

    /**
     * 主键字段名
     */
    public String key() default "id";
}
