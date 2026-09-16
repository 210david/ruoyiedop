package com.ruoyi.dms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.dms.domain.DmsRepairFaq;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsRepairFaqMapper;
import com.ruoyi.dms.mapper.DmsWorkOrderMapper;
import com.ruoyi.dms.service.IDmsAiService;
import com.ruoyi.system.service.ISysConfigService;

@Service
public class DmsAiServiceImpl implements IDmsAiService
{
    private static final Logger log = LoggerFactory.getLogger(DmsAiServiceImpl.class);

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private DmsWorkOrderMapper workOrderMapper;

    @Autowired
    private DmsRepairFaqMapper faqMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private com.ruoyi.ai.core.AiClient aiClient;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ==================== 智能报修助手 ====================

    @Override
    public Map<String, Object> repairAssistant(String input)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("rawInput", input);

        currentFeature.set("repair_assistant");
        if (isAiEnabled("dms.ai.repair_assistant"))
        {
            try
            {
                String prompt = "你是设备故障报修助手。请从以下描述中提取信息，以JSON格式返回：\n"
                        + "- faultDescription: 故障现象描述\n"
                        + "- priority: 优先级(0紧急/1高/2中/3低)\n"
                        + "- reportTime: 发生时间描述\n"
                        + "- equipmentName: 设备名称(如能识别)\n"
                        + "- faultPart: 故障部位\n\n"
                        + "用户输入：" + input;

                String aiResp = callLlm(prompt);
                if (aiResp != null)
                {
                    JSONObject json = parseLlmJson(aiResp);
                    result.put("faultDescription", json.getString("faultDescription"));
                    result.put("priority", json.getString("priority"));
                    result.put("reportTime", json.getString("reportTime"));
                    result.put("equipmentName", json.getString("equipmentName"));
                    result.put("faultPart", json.getString("faultPart"));
                    result.put("aiEnabled", true);
                    result.put("tip", "AI识别结果仅供参考，请确认后提交");
                    return result;
                }
            }
            catch (Exception e)
            {
                log.warn("AI智能报修调用失败，降级为关键词匹配: {}", e.getMessage());
            }
        }

        // 降级：关键词匹配
        return repairAssistantFallback(input, result);
    }

    private Map<String, Object> repairAssistantFallback(String input, Map<String, Object> result)
    {
        if (input.contains("急") || input.contains("紧急") || input.contains("严重"))
            result.put("priority", "0");
        else if (input.contains("快") || input.contains("重要"))
            result.put("priority", "1");
        else
            result.put("priority", "2");

        StringBuilder faultDesc = new StringBuilder();
        if (input.contains("异响") || input.contains("噪音")) faultDesc.append("异响 ");
        if (input.contains("震动") || input.contains("振动")) faultDesc.append("震动 ");
        if (input.contains("漏油")) faultDesc.append("漏油 ");
        if (input.contains("过热") || input.contains("温度高") || input.contains("发热")) faultDesc.append("过热 ");
        if (input.contains("停机") || input.contains("不转") || input.contains("不启动")) faultDesc.append("无法启动 ");
        if (input.contains("报警")) faultDesc.append("报警 ");
        if (faultDesc.length() == 0) faultDesc.append(input);
        result.put("faultDescription", faultDesc.toString().trim());

        if (input.contains("今天上午")) result.put("reportTime", "今天上午");
        else if (input.contains("今天下午")) result.put("reportTime", "今天下午");
        else if (input.contains("昨天")) result.put("reportTime", "昨天");
        else result.put("reportTime", "");

        result.put("aiEnabled", false);
        result.put("tip", "AI功能未开启，使用关键词识别，请确认后提交");
        return result;
    }

    // ==================== 智能派工推荐 ====================

    @Override
    public Map<String, Object> dispatchRecommend(Long orderId)
    {
        // 开关校验：智能派工推荐未开启时返回空推荐
        if (!isAiEnabled("dms.ai.dispatch_recommend"))
        {
            Map<String, Object> off = new HashMap<>();
            off.put("recommendations", new ArrayList<>());
            off.put("tip", "智能派工推荐未开启，请管理员在【AI助手-AI配置】中开启后使用");
            return off;
        }

        DmsWorkOrder order = workOrderMapper.selectWorkOrderById(orderId);
        if (order == null)
        {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "工单不存在");
            return err;
        }

        // 获取所有活跃工单（状态1/2/3）
        DmsWorkOrder query = new DmsWorkOrder();
        List<DmsWorkOrder> allActive = workOrderMapper.selectWorkOrderList(query);

        // 按维修人分组统计
        Map<String, RepairerStats> statsMap = new HashMap<>();
        for (DmsWorkOrder wo : allActive)
        {
            if (wo.getAssigneeName() == null || wo.getAssigneeName().isEmpty()) continue;
            String status = wo.getOrderStatus();
            if (!"1".equals(status) && !"2".equals(status) && !"3".equals(status)) continue;

            RepairerStats stats = statsMap.computeIfAbsent(wo.getAssigneeName(), k -> new RepairerStats());
            stats.name = wo.getAssigneeName();
            stats.currentWorkload++;
        }

        // 统计历史绩效（已验收工单的好评率）和平均响应时长
        for (DmsWorkOrder wo : allActive)
        {
            if (wo.getAssigneeName() == null) continue;
            String status = wo.getOrderStatus();
            if ("5".equals(status))
            {
                RepairerStats stats = statsMap.get(wo.getAssigneeName());
                if (stats == null)
                {
                    stats = new RepairerStats();
                    stats.name = wo.getAssigneeName();
                    statsMap.put(wo.getAssigneeName(), stats);
                }
                stats.completedCount++;
                if (wo.getRating() != null && wo.getRating() >= 4) stats.goodRatingCount++;

                // 计算响应时长（派工→接单）
                if (wo.getAssignTime() != null && wo.getArriveTime() != null)
                {
                    long respMinutes = (wo.getArriveTime().getTime() - wo.getAssignTime().getTime()) / 60000;
                    if (respMinutes >= 0)
                    {
                        stats.totalResponseMinutes += respMinutes;
                        stats.responseCount++;
                    }
                }
            }
        }

        // 计算综合得分：负载50% + 绩效30% + 响应速度20%
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (RepairerStats stats : statsMap.values())
        {
            Map<String, Object> rec = new HashMap<>();
            rec.put("assigneeName", stats.name);
            rec.put("currentWorkload", stats.currentWorkload);

            // 负载得分（越少越高）
            double workloadScore = 1.0 / (1.0 + stats.currentWorkload);

            // 绩效得分
            double performanceScore = stats.completedCount > 0
                    ? (double) stats.goodRatingCount / stats.completedCount : 0.5;

            // 响应速度得分（越快越高，归一化）
            double avgRespMin = stats.responseCount > 0
                    ? (double) stats.totalResponseMinutes / stats.responseCount : 120;
            double responseScore = 60.0 / (60.0 + avgRespMin);

            double totalScore = workloadScore * 0.5 + performanceScore * 0.3 + responseScore * 0.2;
            rec.put("score", BigDecimal.valueOf(totalScore * 100).setScale(1, RoundingMode.HALF_UP).doubleValue());

            // 推荐理由
            List<String> reasons = new ArrayList<>();
            if (stats.currentWorkload == 0) reasons.add("当前无待处理工单");
            else if (stats.currentWorkload <= 2) reasons.add("当前负载较低");
            if (performanceScore >= 0.8 && stats.completedCount > 0) reasons.add("历史好评率高");
            if (responseScore > 0.5) reasons.add("响应速度快");
            if (reasons.isEmpty()) reasons.add("综合评分较高");
            rec.put("reason", String.join("、", reasons));

            recommendations.add(rec);
        }

        // 按得分降序排序
        recommendations.sort((a, b) -> Double.compare(
                (Double) b.get("score"), (Double) a.get("score")));

        // 取前3名
        if (recommendations.size() > 3)
            recommendations = recommendations.subList(0, 3);

        Map<String, Object> result = new HashMap<>();
        result.put("recommendations", recommendations);
        result.put("tip", "推荐基于工单负载(50%)+历史绩效(30%)+响应速度(20%)，管理员可自由选择");
        return result;
    }

    // ==================== 维修知识问答 ====================

    @Override
    public Map<String, Object> knowledgeQA(String question)
    {
        Map<String, Object> result = new HashMap<>();

        // 1. 先搜索FAQ
        DmsRepairFaq faqQuery = new DmsRepairFaq();
        faqQuery.setQuestion(question);
        List<DmsRepairFaq> faqs = faqMapper.selectFaqList(faqQuery);

        if (faqs.isEmpty())
        {
            faqQuery.setQuestion(null);
            faqQuery.setKeywords(question);
            faqs = faqMapper.selectFaqList(faqQuery);
        }

        if (!faqs.isEmpty())
        {
            DmsRepairFaq matched = faqs.get(0);
            result.put("answer", matched.getAnswer());
            result.put("source", "FAQ");
            result.put("faqId", matched.getFaqId());
            matched.setViewCount((matched.getViewCount() != null ? matched.getViewCount() : 0) + 1);
            faqMapper.updateFaq(matched);
        }

        // 2. 搜索历史工单
        DmsWorkOrder orderQuery = new DmsWorkOrder();
        orderQuery.setFaultDescription(question);
        List<DmsWorkOrder> relatedOrders = workOrderMapper.selectWorkOrderList(orderQuery);

        List<Map<String, Object>> refOrders = new ArrayList<>();
        for (DmsWorkOrder wo : relatedOrders)
        {
            if (wo.getOrderStatus() != null && ("4".equals(wo.getOrderStatus()) || "5".equals(wo.getOrderStatus())))
            {
                Map<String, Object> ref = new HashMap<>();
                ref.put("orderNo", wo.getOrderNo());
                ref.put("equipmentName", wo.getEquipmentName());
                ref.put("faultDescription", wo.getFaultDescription());
                ref.put("faultCause", wo.getFaultCause());
                ref.put("repairMeasure", wo.getRepairMeasure());
                refOrders.add(ref);
                if (refOrders.size() >= 5) break;
            }
        }
        result.put("relatedOrders", refOrders);

        // 3. 如果开启AI且无FAQ匹配，调用大模型总结
        if (!result.containsKey("answer") && !refOrders.isEmpty())
        {
            if (isAiEnabled("dms.ai.knowledge_qa"))
            {
                try
                {
                    StringBuilder context = new StringBuilder();
                    for (int i = 0; i < refOrders.size(); i++)
                    {
                        Map<String, Object> ref = refOrders.get(i);
                        context.append(i + 1).append(". 故障：").append(ref.get("faultDescription"))
                                .append("; 原因：").append(ref.get("faultCause"))
                                .append("; 措施：").append(ref.get("repairMeasure")).append("\n");
                    }

                    String prompt = "你是设备维修知识助手。根据以下历史工单记录，回答用户问题。\n"
                            + "历史工单：\n" + context.toString() + "\n"
                            + "用户问题：" + question + "\n"
                            + "请给出可能的原因和处理方法参考，简洁专业。";

                    currentFeature.set("knowledge_qa");
                    String aiResp = callLlm(prompt);
                    if (aiResp != null)
                    {
                        result.put("answer", aiResp);
                        result.put("source", "AI总结(基于历史工单)");
                    }
                }
                catch (Exception e)
                {
                    log.warn("AI知识问答调用失败，降级为文本拼接: {}", e.getMessage());
                }
            }

            // 降级：文本拼接
            if (!result.containsKey("answer"))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("根据历史工单记录，相关问题及处理方法如下：\n\n");
                for (int i = 0; i < refOrders.size(); i++)
                {
                    Map<String, Object> ref = refOrders.get(i);
                    sb.append(i + 1).append(". 故障：").append(ref.get("faultDescription")).append("\n");
                    if (ref.get("faultCause") != null) sb.append("   原因：").append(ref.get("faultCause")).append("\n");
                    if (ref.get("repairMeasure") != null) sb.append("   措施：").append(ref.get("repairMeasure")).append("\n\n");
                }
                result.put("answer", sb.toString());
                result.put("source", "历史工单");
            }
        }

        if (!result.containsKey("answer"))
        {
            // 3.5 FAQ与历史工单均无匹配时，AI直答兜底
            if (isAiEnabled("dms.ai.knowledge_qa"))
            {
                try
                {
                    String prompt = "你是设备维修知识助手。请回答以下设备维修问题：给出可能原因与处理步骤，"
                            + "简洁专业、分点说明；如信息不足请给出建议排查方向。\n用户问题：" + question;
                    currentFeature.set("knowledge_qa");
                    String aiResp = callLlm(prompt);
                    if (aiResp != null)
                    {
                        result.put("answer", aiResp);
                        result.put("source", "AI直答");
                    }
                }
                catch (Exception e)
                {
                    log.warn("AI直答调用失败: {}", e.getMessage());
                }
            }
        }

        if (!result.containsKey("answer"))
        {
            result.put("answer", "暂无相关记录，建议手动排查或联系设备管理员。");
            result.put("source", "无匹配");
        }

        result.put("tip", "以上内容基于历史工单和FAQ生成，仅供参考");
        return result;
    }

    // ==================== AI知识沉淀：工单自动生成FAQ ====================

    @Override
    public Map<String, Object> generateFaqFromOrders(Integer limit)
    {
        Map<String, Object> result = new HashMap<>();
        if (!isAiEnabled("dms.ai.knowledge_qa"))
        {
            result.put("success", false);
            result.put("msg", "AI功能未开启，请先在【AI配置】中开启总开关与维修知识问答");
            return result;
        }

        int max = (limit != null && limit > 0 && limit <= 20) ? limit : 8;

        // 已有FAQ中引用过的工单号，避免重复生成
        List<DmsRepairFaq> existFaqs = faqMapper.selectFaqList(new DmsRepairFaq());
        List<String> usedRemarks = new ArrayList<>();
        for (DmsRepairFaq f : existFaqs)
        {
            if (f.getRemark() != null && f.getRemark().contains("来源工单:"))
            {
                usedRemarks.add(f.getRemark());
            }
        }

        // 筛选可沉淀的完工/验收工单：有故障原因和维修措施且未被引用过
        List<DmsWorkOrder> candidates = new ArrayList<>();
        DmsWorkOrder query = new DmsWorkOrder();
        List<DmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(query);
        for (DmsWorkOrder wo : allOrders)
        {
            String status = wo.getOrderStatus();
            if (!"4".equals(status) && !"5".equals(status)) continue;
            if (StringUtils.isEmpty(wo.getFaultDescription()) || StringUtils.isEmpty(wo.getRepairMeasure())) continue;
            if (wo.getOrderNo() != null)
            {
                boolean used = false;
                for (String remark : usedRemarks)
                {
                    if (remark.contains(wo.getOrderNo())) { used = true; break; }
                }
                if (used) continue;
            }
            candidates.add(wo);
            if (candidates.size() >= max) break;
        }

        if (candidates.isEmpty())
        {
            result.put("success", true);
            result.put("generated", 0);
            result.put("msg", "没有可沉淀的新完工工单（可能均已生成过或缺少故障原因/维修措施）");
            return result;
        }

        // 构建Prompt，要求返回JSON数组
        StringBuilder sb = new StringBuilder();
        sb.append("以下是设备维修完工工单记录。请将每条工单沉淀为一条维修知识FAQ，以JSON数组返回，不要输出其他内容。\n")
          .append("数组元素字段：question(问题，以设备+故障现象描述)、answer(标准处理答案，含原因分析与处理步骤，简洁专业)、keywords(关键词，逗号分隔)。\n\n");
        for (int i = 0; i < candidates.size(); i++)
        {
            DmsWorkOrder wo = candidates.get(i);
            sb.append(i + 1).append(". 工单号：").append(wo.getOrderNo())
              .append("；设备：").append(wo.getEquipmentName())
              .append("；故障现象：").append(wo.getFaultDescription())
              .append("；故障原因：").append(StringUtils.nvl(wo.getFaultCause(), "未记录"))
              .append("；维修措施：").append(wo.getRepairMeasure()).append("\n");
        }

        currentFeature.set("faq_generate");
        String aiResp = callLlm(sb.toString());
        if (aiResp == null)
        {
            result.put("success", false);
            result.put("msg", "AI调用失败，请检查AI配置（API地址/密钥/月度限额）");
            return result;
        }

        // 解析JSON数组（兼容Markdown包裹）
        String cleaned = aiResp.trim();
        if (cleaned.startsWith("```"))
        {
            int start = cleaned.indexOf('\n');
            int end = cleaned.lastIndexOf("```");
            if (start > 0 && end > start) cleaned = cleaned.substring(start + 1, end).trim();
        }
        int arrStart = cleaned.indexOf('[');
        int arrEnd = cleaned.lastIndexOf(']');
        if (arrStart < 0 || arrEnd <= arrStart)
        {
            result.put("success", false);
            result.put("msg", "AI返回格式异常，请重试");
            return result;
        }
        JSONArray arr = JSON.parseArray(cleaned.substring(arrStart, arrEnd + 1));

        int generated = 0;
        for (int i = 0; i < arr.size() && i < candidates.size(); i++)
        {
            JSONObject obj = arr.getJSONObject(i);
            if (obj == null || StringUtils.isEmpty(obj.getString("question"))) continue;
            DmsRepairFaq faq = new DmsRepairFaq();
            faq.setQuestion(obj.getString("question"));
            faq.setAnswer(obj.getString("answer"));
            faq.setKeywords(obj.getString("keywords"));
            faq.setStatus("1"); // 待人工审核，审核通过后启用
            faq.setRemark("AI生成-待审核 来源工单:" + candidates.get(i).getOrderNo());
            faq.setCreateBy("ai");
            faqMapper.insertFaq(faq);
            generated++;
        }

        result.put("success", true);
        result.put("generated", generated);
        result.put("msg", "已从完工工单生成 " + generated + " 条FAQ，状态为待审核，请审核后启用");
        return result;
    }

    // ==================== 大模型API调用 ====================

    /**
     * 判断AI功能是否启用
     */
    private boolean isAiEnabled(String configKey)
    {
        // 迁移至ruoyi-ai公共模块：三级开关校验（平台/模块/功能），兼容旧键
        String feature = configKey.replace("dms.ai.", "");
        return aiClient.isEnabled("dms", feature);
    }

    /**
     * 调用大模型API（迁移至ruoyi-ai统一客户端，含限额/脱敏/审计）
     * 返回content，失败返回null（与原行为一致，调用方走降级逻辑）
     */
    private String callLlm(String prompt)
    {
        com.ruoyi.ai.core.AiResult result = aiClient.chat(currentModule.get(), currentFeature.get(), prompt);
        return result.isSuccess() ? result.getContent() : null;
    }

    /** AiClient上下文（调用前由各功能入口设置module/feature） */
    private static final ThreadLocal<String> currentModule = ThreadLocal.withInitial(() -> "dms");
    private static final ThreadLocal<String> currentFeature = ThreadLocal.withInitial(() -> "unknown");

    /**
     * 解析月度限额配置（空或非法值视为不限制）
     */
    private int parseMonthlyLimit(String val)
    {
        if (StringUtils.isEmpty(val)) return 0;
        try
        {
            return Integer.parseInt(val.trim());
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    /**
     * 解析大模型返回的JSON（兼容Markdown代码块包裹）
     */
    private JSONObject parseLlmJson(String text)
    {
        if (text == null) return new JSONObject();
        // 去除可能的 ```json ... ``` 包裹
        String cleaned = text.trim();
        if (cleaned.startsWith("```"))
        {
            int start = cleaned.indexOf('\n');
            int end = cleaned.lastIndexOf("```");
            if (start > 0 && end > start)
            {
                cleaned = cleaned.substring(start + 1, end).trim();
            }
        }
        try
        {
            return JSON.parseObject(cleaned);
        }
        catch (Exception e)
        {
            log.warn("解析AI返回JSON失败: {}", text);
            return new JSONObject();
        }
    }

    /** 截断过长文本，避免错误信息刷屏 */
    private String abbreviate(String text, int maxLen)
    {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    /** 维修员统计数据 */
    private static class RepairerStats
    {
        String name;
        int currentWorkload = 0;
        int completedCount = 0;
        int goodRatingCount = 0;
        long totalResponseMinutes = 0;
        int responseCount = 0;
    }
}
