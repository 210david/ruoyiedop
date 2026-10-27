package com.ruoyi.ai.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * NL2Query 自然语言查数 Service接口
 * 自然语言 → LLM生成可查范围内只读SQL → 校验 → 执行 → 表格返回
 *
 * @author ruoyi
 */
public interface IAiNlQueryService
{
    /** 自然语言查询：返回 columns/rows/sql 或 error */
    Map<String, Object> query(String question);

    /**
     * 自然语言查询（带阶段回调）：自动捕获登录人权限与上下文后执行
     *
     * @param question 用户问题
     * @param stage 阶段回调：参数1=阶段名(tables/sql/exec)，参数2=状态(start/done)，可为null
     * @return columns/rows/sql 或 error
     */
    Map<String, Object> query(String question, BiConsumer<String, String> stage);

    /**
     * 带阶段回调的自然语言查询（供流式接口推送进度）
     *
     * @param question 用户问题
     * @param userPerms 当前登录人权限串集合（按权限裁剪可查表）
     * @param userInfo 当前登录人上下文描述（如"账号:admin，昵称:管理员，部门:总公司"），可为null
     * @param stage 阶段回调：参数1=阶段名(understand/tables/sql/exec/summary)，参数2=状态(start/done)
     */
    Map<String, Object> queryWithStages(String question, Set<String> userPerms, String userInfo, BiConsumer<String, String> stage);

    /**
     * AI结论生成：把查询结果回喂给大模型，生成面向业务用户的简短中文结论（说人话）。
     * 供 NL2SQL 与工具路由两条链路复用；AI调用失败时返回null（前端降级为纯表格展示）。
     *
     * @param question 用户原始问题
     * @param columns 结果列名（已中文化）
     * @param rows 结果行（可为空列表，此时生成"无数据"结论）
     * @return 自然语言结论；失败返回null
     */
    String summarizeResult(String question, List<String> columns, List<Map<String, Object>> rows);
}
