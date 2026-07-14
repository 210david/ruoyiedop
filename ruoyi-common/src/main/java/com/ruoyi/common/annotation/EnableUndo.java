package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作撤销注解，标记支持撤销的方法
 * 方法执行前自动保存数据快照到 sys_undo_log 表
 * 
 * @author ruoyi
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableUndo
{
    /**
     * 实体类型（表名，用于回查定位）
     */
    public String entityType() default "";

    /**
     * 主键字段名，用于从方法参数中提取实体ID
     */
    public String key() default "id";

    /**
     * 操作类型描述
     */
    public String operateType() default "UPDATE";
}
