package com.ruoyi.quartz.task;

import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;

/**
 * 定时任务调度测试
 * 
 * @author ruoyi
 */
@Component("ryTask")
public class RyTask
{
    /**
     * 多参数测试方法
     * 【生产环境建议禁用】此方法仅为定时任务调度功能测试，无实际业务用途
     */
    public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        System.out.println(StringUtils.format("执行多参方法： 字符串类型{}，布尔类型{}，长整型{}，浮点型{}，整形{}", s, b, l, d, i));
    }

    /**
     * 有参测试方法
     * 【生产环境建议禁用】此方法仅为定时任务调度功能测试，无实际业务用途
     */
    public void ryParams(String params)
    {
        System.out.println("执行有参方法：" + params);
    }

    /**
     * 无参测试方法
     * 【生产环境建议禁用】此方法仅为定时任务调度功能测试，无实际业务用途
     */
    public void ryNoParams()
    {
        System.out.println("执行无参方法");
    }
}
