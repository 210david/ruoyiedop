package com.ruoyi.ai.service;

import java.util.Map;

/**
 * NL2Query 自然语言查数 Service接口
 * 自然语言 → LLM生成白名单内只读SQL → 校验 → 执行 → 表格返回
 *
 * @author ruoyi
 */
public interface IAiNlQueryService
{
    /** 自然语言查询：返回 columns/rows/sql 或 error */
    Map<String, Object> query(String question);
}
