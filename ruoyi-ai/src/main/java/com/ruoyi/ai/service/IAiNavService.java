package com.ruoyi.ai.service;

import java.util.List;
import java.util.Map;

/**
 * AI导航 Service：把「用户问题」解析为可点击的系统菜单入口
 *
 * 设计原则（与 AiToolRoute 一致）：
 * 1. 确定性优先：知识文档绑定菜单 / 规则别名命中，零模型成本
 * 2. 模型兜底：仅当规则未命中时才调 LLM，且 LLM 只能选 intentKey，不能输出路径
 * 3. 权限实时裁剪：路径由 menu_id + 登录人菜单树计算，不接受外部传入路径
 * 4. 失败降级：任何异常都返回空列表，绝不阻断主答案
 *
 * @author ruoyi
 */
public interface IAiNavService
{
    /**
     * 解析导航入口（通用：数据查询 / 知识问答共用）
     *
     * @param question 用户问题
     * @return 可点击入口列表，无匹配返回空列表（不阻断主流程）
     */
    List<Map<String, Object>> resolve(String question);

    /**
     * 解析导航入口（知识问答专用，优先按来源文档绑定直连）
     *
     * @param question 用户问题
     * @param sources  知识库来源 [{docId, docName, module}]
     * @return 可点击入口列表
     */
    List<Map<String, Object>> resolveBySources(String question, List<Map<String, Object>> sources);

    /** 当前登录人有权访问的菜单索引：menuId → 路径信息（自查/管理界面用） */
    Map<Long, Map<String, Object>> currentUserMenuIndex();

    /** 导航开关是否开启 */
    boolean navEnabled();
}
