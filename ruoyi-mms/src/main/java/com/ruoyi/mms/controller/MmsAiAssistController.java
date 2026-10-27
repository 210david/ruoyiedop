package com.ruoyi.mms.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mms.domain.MmsAbnormal;
import com.ruoyi.mms.domain.MmsStatDaily;
import com.ruoyi.mms.service.IMmsAbnormalService;
import com.ruoyi.mms.service.IMmsStatDailyService;

/**
 * MMS AI辅助 Controller（外挂式，不改动既有业务代码）
 * P0功能：生产异常智能诊断+处置建议 + 生产日报自动生成
 * AI输出均为"草稿/建议"，落库前必须人工确认
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/ai")
public class MmsAiAssistController extends BaseController
{
    @Autowired
    private AiClient aiClient;

    @Autowired
    private IMmsAbnormalService abnormalService;

    @Autowired
    private IMmsStatDailyService statDailyService;

    /**
     * 生产异常智能诊断：基于异常记录生成原因分析+处置建议
     */
    @PreAuthorize("@ss.hasPermi('mms:ai:abnormalAdvice')")
    @GetMapping("/abnormalAdvice/{abnormalId}")
    public AjaxResult abnormalAdvice(@PathVariable Long abnormalId)
    {
        MmsAbnormal abnormal = abnormalService.selectAbnormalById(abnormalId);
        if (abnormal == null)
        {
            return AjaxResult.error("异常记录不存在");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是生产异常诊断助手。根据以下生产异常记录，给出诊断分析和处置建议。\n")
              .append("请以JSON格式返回：possibleCauses(可能原因数组，按可能性排序)、\n")
              .append("urgentAction(紧急处置建议)、rootCauseHint(根因排查方向)、\n")
              .append("preventiveAdvice(预防再发建议)、riskLevel(建议关注等级：高/中/低)。\n\n")
              .append("【异常记录】\n")
              .append("异常单号：").append(StringUtils.nvl(abnormal.getAbnormalNo(), "")).append("\n")
              .append("工单号：").append(StringUtils.nvl(abnormal.getWorkOrderNo(), "")).append("\n")
              .append("产能单元：").append(StringUtils.nvl(abnormal.getResourceName(), "")).append("\n")
              .append("产品：").append(StringUtils.nvl(abnormal.getProductName(), "")).append("\n")
              .append("异常类型：").append(abnormalTypeText(abnormal.getAbnormalType())).append("\n")
              .append("严重等级：").append(severityText(abnormal.getSeverity())).append("\n")
              .append("影响范围：").append(StringUtils.nvl(abnormal.getImpactScope(), "未填写")).append("\n")
              .append("异常描述：").append(StringUtils.nvl(abnormal.getDescription(), "未填写")).append("\n")
              .append("已采取措施：").append(StringUtils.nvl(abnormal.getResponseMeasure(), "未填写")).append("\n")
              .append("停机时长(小时)：").append(abnormal.getDowntimeHours() == null ? "" : abnormal.getDowntimeHours().toString()).append("\n");

        AiResult result = aiClient.chatCached("mms", "abnormal_advice", prompt.toString());
        Map<String, Object> data = new HashMap<>(result.toMap());
        if (result.isSuccess())
        {
            data.put("parsed", com.ruoyi.ai.util.AiJsonParser.parse(result.getContent()));
        }
        return AjaxResult.success(data);
    }

    /**
     * 生产日报生成：基于当日统计快照生成管理视角日报草稿
     */
    @PreAuthorize("@ss.hasPermi('mms:ai:dailyReport')")
    @GetMapping("/dailyReport/{statDate}")
    public AjaxResult dailyReport(@PathVariable String statDate)
    {
        MmsStatDaily stat = statDailyService.selectStatDailyByDate(statDate);
        if (stat == null)
        {
            return AjaxResult.error("当日无生产统计数据，无法生成日报");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是生产管理报告撰写助手。根据以下当日生产统计数据，生成一份面向管理层的生产日报草稿。\n")
              .append("结构：一、生产概况 二、计划达成 三、质量表现 四、异常情况 五、关注建议。\n")
              .append("要求：结论先行，数字准确引用，最后给出1-3条管理关注建议。\n\n")
              .append("【当日统计】\n")
              .append("日期：").append(stat.getStatDate() == null ? "" : stat.getStatDate().toString()).append("\n")
              .append("工单数：").append(nvlInt(stat.getWorkOrderCount())).append("\n")
              .append("已完工数：").append(nvlInt(stat.getCompletedCount())).append("\n")
              .append("在制数：").append(nvlInt(stat.getInProgressCount())).append("\n")
              .append("异常数：").append(nvlInt(stat.getAbnormalCount())).append("\n")
              .append("计划数量：").append(nvlDec(stat.getTotalPlanQty())).append("\n")
              .append("合格数量：").append(nvlDec(stat.getTotalGoodQty())).append("\n")
              .append("不良数量：").append(nvlDec(stat.getTotalDefectQty())).append("\n")
              .append("平均OEE：").append(nvlDec(stat.getAvgOee())).append("\n");

        AiResult result = aiClient.chatCached("mms", "daily_report", prompt.toString());
        return AjaxResult.success(result.toMap());
    }

    private String abnormalTypeText(String type)
    {
        switch (type == null ? "" : type)
        {
            case "0": return "设备";
            case "1": return "物料";
            case "2": return "质量";
            case "3": return "安全";
            default: return "其他";
        }
    }

    private String severityText(String severity)
    {
        switch (severity == null ? "" : severity)
        {
            case "0": return "一般";
            case "1": return "严重";
            case "2": return "紧急";
            default: return severity;
        }
    }

    private String nvlInt(Integer v) { return v == null ? "0" : v.toString(); }
    private String nvlDec(java.math.BigDecimal v) { return v == null ? "0" : v.toPlainString(); }
}
