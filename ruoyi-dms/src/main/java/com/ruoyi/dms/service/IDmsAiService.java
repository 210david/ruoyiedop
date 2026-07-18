package com.ruoyi.dms.service;

import java.util.List;
import java.util.Map;

/**
 * AI大模型服务接口
 * 支持智能报修、派工推荐、知识问答
 * 可通过系统参数开关控制，关闭时降级为关键词匹配
 */
public interface IDmsAiService
{
    /**
     * 智能报修助手：自然语言→结构化字段
     * 优先调用大模型API，失败时降级为关键词匹配
     */
    Map<String, Object> repairAssistant(String input);

    /**
     * 智能派工推荐：基于负载50%+绩效30%+响应速度20%
     */
    Map<String, Object> dispatchRecommend(Long orderId);

    /**
     * 维修知识问答：历史工单检索+大模型总结
     */
    Map<String, Object> knowledgeQA(String question);
}
