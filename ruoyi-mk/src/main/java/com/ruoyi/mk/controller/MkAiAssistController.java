package com.ruoyi.mk.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkOrder;
import com.ruoyi.mk.service.IMkCustomerService;
import com.ruoyi.mk.service.IMkOrderService;

/**
 * 营销 AI辅助 Controller（外挂式，不改动既有业务代码）
 * 功能：客户流失预警报告 + 销售周报生成
 * AI输出均为"草稿/建议"，仅供决策参考
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/ai")
public class MkAiAssistController extends BaseController
{
    /** 客户流失分析：最多取前N条客户数据组装prompt */
    private static final int CUSTOMER_LIMIT = 100;

    /** 周报统计窗口：近N天订单 */
    private static final int WEEKLY_DAYS = 7;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private IMkCustomerService customerService;

    @Autowired
    private IMkOrderService orderService;

    /**
     * 客户流失预警报告：拉取客户清单，LLM分析客户等级分布、流失风险分级与挽回建议
     */
    @PreAuthorize("@ss.hasPermi('mk:ai:churnReport')")
    @GetMapping("/customerChurnReport")
    public AjaxResult customerChurnReport()
    {
        List<MkCustomer> list = customerService.selectCustomerList(new MkCustomer());
        if (list == null || list.isEmpty())
        {
            return AjaxResult.error("暂无客户数据");
        }
        StringBuilder data = new StringBuilder();
        int count = 0;
        for (MkCustomer c : list)
        {
            if (count++ >= CUSTOMER_LIMIT) break;
            data.append(StringUtils.nvl(c.getCustomerNo(), "-"))
                .append(" | 企业名称:").append(StringUtils.nvl(c.getCustomerName(), "-"))
                .append(" | 等级:").append(StringUtils.nvl(c.getCustomerLevel(), "-"))
                .append(" | 状态:").append(StringUtils.nvl(c.getCustomerStatus(), "-"))
                .append(" | 首次接触:").append(c.getFirstContactDate() == null ? "-" : c.getFirstContactDate())
                .append(" | 合作开始:").append(c.getCooperationDate() == null ? "-" : c.getCooperationDate())
                .append(" | 累计交易额:").append(c.getTotalAmount() == null ? "0" : c.getTotalAmount().toPlainString())
                .append(" | 负责人:").append(StringUtils.nvl(c.getUserName(), "-"));
            data.append("\n");
        }

        String prompt = "你是客户运营分析助手。根据以下客户清单（共" + list.size() + "个客户，展示前" + count + "条），生成客户流失预警报告。\n"
                + "结构：一、客户等级与状态分布概况 二、流失风险分级（按高/中/低三档列出重点客户名单，结合客户状态、合作开始时间、累计交易额判断）"
                + " 三、挽回建议（针对高风险客户给出具体可执行的挽回动作）。\n"
                + "要求：结论先行，引用具体企业名称；数据中缺失的信息一律写【待补充】，不得编造。\n\n"
                + "【客户清单（前" + count + "条）】\n" + data;

        AiResult result = aiClient.chatCached("mk", "customer_churn_report", prompt);
        return AjaxResult.success(result.toMap());
    }

    /**
     * 销售周报生成：拉取近7天订单，LLM生成本周销售周报；无订单时生成模板周报
     */
    @PreAuthorize("@ss.hasPermi('mk:ai:weeklyReport')")
    @GetMapping("/weeklyReport")
    public AjaxResult weeklyReport()
    {
        List<MkOrder> all = orderService.selectOrderList(new MkOrder());
        long deadline = System.currentTimeMillis() - WEEKLY_DAYS * 86400000L;
        StringBuilder data = new StringBuilder();
        int orderCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (all != null)
        {
            for (MkOrder o : all)
            {
                Date create = o.getCreateTime();
                if (create == null || create.getTime() < deadline)
                {
                    continue;
                }
                orderCount++;
                if (o.getOrderAmount() != null)
                {
                    totalAmount = totalAmount.add(o.getOrderAmount());
                }
                data.append(StringUtils.nvl(o.getOrderNo(), "-"))
                    .append(" | 客户:").append(StringUtils.nvl(o.getCustomerName(), "-"))
                    .append(" | 金额:").append(o.getOrderAmount() == null ? "0" : o.getOrderAmount().toPlainString())
                    .append(" | 状态:").append(StringUtils.nvl(o.getOrderStatus(), "-"))
                    .append(" | 下单时间:").append(create)
                    .append(" | 负责人:").append(StringUtils.nvl(o.getUserName(), "-"));
                data.append("\n");
            }
        }

        String prompt;
        if (orderCount == 0)
        {
            prompt = "你是销售运营助手。本期（近" + WEEKLY_DAYS + "天）没有任何订单记录。"
                    + "请生成一份销售周报模板，包含：一、成交概况（写明本期无成交订单） 二、销售金额（0）"
                    + " 三、重点客户（写【待补充】，由销售手工填写本周跟进客户） 四、风险提示与下周计划（给出模板化建议框架）。"
                    + "不得编造任何订单或客户数据。";
        }
        else
        {
            prompt = "你是销售运营助手。根据以下近" + WEEKLY_DAYS + "天的订单数据（共" + orderCount
                    + "笔，合计金额" + totalAmount.toPlainString() + "），生成销售周报。\n"
                    + "结构：一、成交概况（笔数、金额、状态分布） 二、金额分析（客单价/大额订单）"
                    + " 三、重点客户（按金额与频次列出） 四、风险提示（如待审核积压、作废/驳回单、长期未发货）。\n"
                    + "要求：结论先行，引用具体订单号与客户名称；数据中缺失的信息一律写【待补充】，不得编造。\n\n"
                    + "【近" + WEEKLY_DAYS + "天订单明细】\n" + data;
        }

        AiResult result = aiClient.chatCached("mk", "sales_weekly_report", prompt);
        return AjaxResult.success(result.toMap());
    }
}
