package com.ruoyi.pms.controller;

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
import com.ruoyi.pms.domain.PmsInquiry;
import com.ruoyi.pms.domain.PmsInquiryDetail;
import com.ruoyi.pms.domain.PmsPurchaseRequest;
import com.ruoyi.pms.domain.PmsPurchaseRequestDetail;
import com.ruoyi.pms.service.IPmsInquiryService;
import com.ruoyi.pms.service.IPmsPurchaseRequestService;

/**
 * PMS AI辅助 Controller（外挂式，不改动既有业务代码）
 * P0功能：智能采购建议（需求草拟辅助）+ 报价对比分析摘要
 * AI输出均为"草稿/建议"，落库前必须人工确认
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/ai")
public class PmsAiAssistController extends BaseController
{
    @Autowired
    private AiClient aiClient;

    @Autowired
    private IPmsPurchaseRequestService requestService;

    @Autowired
    private IPmsInquiryService inquiryService;

    /**
     * 采购申请AI建议：辅助确认需求合理性（是否建议转采购计划）
     */
    @PreAuthorize("@ss.hasPermi('pms:request:query')")
    @GetMapping("/requestAdvice/{requestId}")
    public AjaxResult requestAdvice(@PathVariable Long requestId)
    {
        PmsPurchaseRequest request = requestService.selectPurchaseRequestById(requestId);
        if (request == null)
        {
            return AjaxResult.error("采购申请不存在");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是采购决策助手。根据以下采购申请信息，给出采购确认建议。\n")
              .append("请以JSON格式返回：suggestion(建议：proceed推进/hold暂缓/split拆分采购)、\n")
              .append("reason(推荐理由)、risk(风险提示)、notes(补充建议如询价策略)。\n\n")
              .append("【采购申请】\n")
              .append("申请单号：").append(StringUtils.nvl(request.getRequestNo(), "")).append("\n")
              .append("申请标题：").append(StringUtils.nvl(request.getTitle(), "")).append("\n")
              .append("优先级：").append(priorityText(request.getPriority())).append("\n")
              .append("采购用途：").append(StringUtils.nvl(request.getPurpose(), "未填写")).append("\n")
              .append("预估总金额：").append(request.getTotalAmount() == null ? "" : request.getTotalAmount().toPlainString()).append("\n")
              .append("期望交货：").append(request.getExpectDate() == null ? "" : request.getExpectDate().toString()).append("\n");

        if (request.getDetailList() != null && !request.getDetailList().isEmpty())
        {
            prompt.append("【明细】\n");
            int i = 1;
            for (PmsPurchaseRequestDetail d : request.getDetailList())
            {
                prompt.append(i++).append(". ").append(StringUtils.nvl(d.getMaterialName(), ""))
                      .append(" 数量：").append(d.getQty() == null ? "" : d.getQty().toPlainString())
                      .append(" 预估单价：").append(d.getEstimatedPrice() == null ? "" : d.getEstimatedPrice().toPlainString())
                      .append("\n");
            }
        }

        AiResult result = aiClient.chat("pms", "request_advice", prompt.toString());
        Map<String, Object> data = new HashMap<>(result.toMap());
        if (result.isSuccess())
        {
            data.put("parsed", com.ruoyi.ai.util.AiJsonParser.parse(result.getContent()));
        }
        return AjaxResult.success(data);
    }

    /**
     * 询价比价AI摘要：辅助多份报价对比决策
     */
    @PreAuthorize("@ss.hasPermi('pms:inquiry:query')")
    @GetMapping("/priceSummary/{inquiryId}")
    public AjaxResult priceSummary(@PathVariable Long inquiryId)
    {
        PmsInquiry inquiry = inquiryService.selectPmsInquiryById(inquiryId);
        if (inquiry == null)
        {
            return AjaxResult.error("询价单不存在");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是采购比价分析助手。根据以下询价单信息，生成比价决策分析摘要。\n")
              .append("结构：1.询价概况 2.比价要点 3.定标建议与理由 4.风险提示（付款/交期/供应商集中度）。\n")
              .append("注意：具体报价数据请留【待填写】占位，由采购员从比价表补全；你的价值在于给出评估框架和关注点。\n\n")
              .append("【询价单】\n")
              .append("询价单号：").append(StringUtils.nvl(inquiry.getInquiryNo(), "")).append("\n")
              .append("询价标题：").append(StringUtils.nvl(inquiry.getTitle(), "")).append("\n")
              .append("询价方式：").append("1".equals(inquiry.getInquiryType()) ? "邀请询价" : "公开询价").append("\n")
              .append("采购类型：").append(purchaseTypeText(inquiry.getPurchaseType())).append("\n")
              .append("预计金额：").append(inquiry.getEstimateAmount() == null ? "" : inquiry.getEstimateAmount().toPlainString()).append("\n")
              .append("报价截止：").append(inquiry.getDeadline() == null ? "" : inquiry.getDeadline().toString()).append("\n");

        if (inquiry.getDetailList() != null)
        {
            prompt.append("【询价明细】\n");
            int i = 1;
            for (PmsInquiryDetail d : inquiry.getDetailList())
            {
                prompt.append(i++).append(". ").append(StringUtils.nvl(d.getMaterialName(), ""))
                      .append(" 数量：").append(d.getQuantity() == null ? "" : d.getQuantity().toPlainString())
                      .append(" 最高限价：").append(d.getMaxPrice() == null ? "无" : d.getMaxPrice().toPlainString())
                      .append("\n");
            }
        }

        AiResult result = aiClient.chat("pms", "price_summary", prompt.toString());
        return AjaxResult.success(result.toMap());
    }

    private String priorityText(String priority)
    {
        switch (priority == null ? "" : priority)
        {
            case "0": return "普通";
            case "1": return "紧急";
            case "2": return "特急";
            default: return priority;
        }
    }

    private String purchaseTypeText(String type)
    {
        switch (type == null ? "" : type)
        {
            case "0": return "物资";
            case "1": return "服务";
            case "2": return "工程";
            default: return type;
        }
    }
}
