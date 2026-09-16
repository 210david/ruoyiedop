package com.ruoyi.qms.controller;

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
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.service.IQmsNcrService;

/**
 * QMS AI辅助 Controller（外挂式，不改动既有业务代码）
 * P0功能：NCR不合格品处置建议 + 质量报告初稿生成
 * AI输出均为"草稿/建议"，落库前必须人工确认
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/ai")
public class QmsAiAssistController extends BaseController
{
    @Autowired
    private AiClient aiClient;

    @Autowired
    private IQmsNcrService ncrService;

    /**
     * NCR处置建议：基于缺陷描述+历史处置记录生成处置方式建议
     */
    @PreAuthorize("@ss.hasPermi('qms:ncr:query')")
    @GetMapping("/ncrAdvice/{ncrId}")
    public AjaxResult ncrAdvice(@PathVariable Long ncrId)
    {
        QmsNcr ncr = ncrService.selectNcrById(ncrId);
        if (ncr == null)
        {
            return AjaxResult.error("NCR不存在");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是质量管理处置助手。根据以下不合格品信息，给出处置方式建议。\n")
              .append("处置方式枚举：1退货/2挑选/3返工/4报废/5让步接收。\n")
              .append("请以JSON格式返回：disposition(建议处置方式代码)、reason(推荐理由)、\n")
              .append("risk(风险提示)、isolation(是否建议隔离true/false)、notes(补充说明)。\n\n")
              .append("【不合格品信息】\n")
              .append("NCR编号：").append(StringUtils.nvl(ncr.getNcrNo(), "")).append("\n")
              .append("物料名称：").append(StringUtils.nvl(ncr.getMaterialName(), "")).append("\n")
              .append("供应商：").append(StringUtils.nvl(ncr.getSupplierName(), "")).append("\n")
              .append("批次号：").append(StringUtils.nvl(ncr.getBatchNo(), "")).append("\n")
              .append("不合格数量：").append(ncr.getDefectQty() == null ? "" : ncr.getDefectQty().toString()).append("\n")
              .append("缺陷等级：").append(defectLevelText(ncr.getDefectLevel())).append("\n")
              .append("缺陷描述：").append(StringUtils.nvl(ncr.getDefectDesc(), "未填写")).append("\n");

        AiResult result = aiClient.chat("qms", "ncr_advice", prompt.toString());
        Map<String, Object> data = new HashMap<>(result.toMap());
        if (result.isSuccess())
        {
            data.put("parsed", com.ruoyi.ai.util.AiJsonParser.parse(result.getContent()));
        }
        return AjaxResult.success(data);
    }

    /**
     * 质量报告初稿生成：基于给定期间数据生成报告草稿
     * body: { "beginDate": "2026-09-01", "endDate": "2026-09-30" }
     */
    @PreAuthorize("@ss.hasPermi('qms:report:query')")
    @PostMapping("/reportDraft")
    public AjaxResult reportDraft(@RequestBody Map<String, String> body)
    {
        String beginDate = body.getOrDefault("beginDate", "");
        String endDate = body.getOrDefault("endDate", "");
        if (StringUtils.isEmpty(beginDate) || StringUtils.isEmpty(endDate))
        {
            return AjaxResult.error("请提供报告期间（beginDate/endDate）");
        }

        String prompt = "你是质量报告撰写助手。请生成一份质量月报初稿，结构包含：\n"
                + "一、质量概况 二、检验情况 三、不合格与处置 四、客诉情况 五、CAPA进展 六、改进建议。\n"
                + "要求：结构清晰、语言专业；数据部分请留【待填写】占位，由质量员从系统报表补全。\n"
                + "报告期间：" + beginDate + " 至 " + endDate + "。";

        AiResult result = aiClient.chat("qms", "report_draft", prompt);
        return AjaxResult.success(result.toMap());
    }

    private String defectLevelText(String level)
    {
        switch (level == null ? "" : level)
        {
            case "1": return "致命";
            case "2": return "严重";
            case "3": return "一般";
            case "4": return "轻微";
            default: return level;
        }
    }
}
