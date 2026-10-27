package com.ruoyi.ai.service;

import java.util.Map;

/**
 * AI工具路由 Service接口（方案3：Text-to-API）
 * 高频问题工具直答 + 长尾降级NL2SQL
 *
 * @author ruoyi
 */
public interface IAiToolRouteService
{
    /**
     * 智能查询统一入口：先工具路由（快、准），无匹配降级NL2SQL（慢、活）
     *
     * @param question 用户自然语言问题
     * @return columns/rows/total/sql/tool（工具命中时）或NL2SQL结果
     */
    public Map<String, Object> query(String question);

    /**
     * 带阶段回调的统一入口：阶段回调透传给NL2SQL（供流式接口推送真实进度）
     *
     * @param question 用户自然语言问题
     * @param stage 阶段回调：参数1=阶段名(tables/sql/exec)，参数2=状态(start/done)，可为null
     * @return columns/rows/total/sql/tool（工具命中时）或NL2SQL结果
     */
    public Map<String, Object> query(String question, java.util.function.BiConsumer<String, String> stage);
}
