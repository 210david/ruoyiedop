package com.ruoyi.ai.service;

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
     * 带阶段回调的自然语言查询（供流式接口推送进度）
     *
     * @param question 用户问题
     * @param userPerms 当前登录人权限串集合（按权限裁剪可查表）
     * @param userInfo 当前登录人上下文描述（如"账号:admin，昵称:管理员，部门:总公司"），可为null
     * @param stage 阶段回调：参数1=阶段名(understand/tables/sql/exec)，参数2=状态(start/done)
     */
    Map<String, Object> queryWithStages(String question, Set<String> userPerms, String userInfo, BiConsumer<String, String> stage);
}
