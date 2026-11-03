package com.ruoyi.mk.controller;

import com.ruoyi.common.utils.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkDealerProfile;
import com.ruoyi.mk.domain.MkDealerSellFlow;
import com.ruoyi.mk.domain.MkOrder;
import com.ruoyi.mk.service.IMkCustomerService;
import com.ruoyi.mk.service.IMkDealerProfileService;
import com.ruoyi.mk.service.IMkDealerSellFlowService;
import com.ruoyi.mk.service.IMkOrderService;

/**
 * MK 客户流失预测 AI Controller（外挂式，不改动既有业务代码）
 * P2功能：客户流失预测（规则打分：互动时效+订单趋势+价值分层，LLM总结）
 * P2-03扩展：流失预警增加经销商维度规则与输出字段
 *
 * 流失风险规则（0-100，越高越危险）：
 *   近180天无订单 +40；近90天无订单 +20；累计订单0单 +15；合作超1年但无复购 +10
 *   高价值客户（累计金额Top20%）近90天无订单 额外 +15
 * 经销商维度附加规则（customer_type='2'）：
 *   未结返利>0（返利纠纷流失信号）+10；流向登记中断（最近流向超90天或从未登记）+10；信用冻结（资金链风险）+15
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/ai")
public class MkPredictController extends BaseController
{
    @Autowired
    private AiClient aiClient;

    @Autowired
    private IMkCustomerService customerService;

    @Autowired
    private IMkOrderService orderService;

    @Autowired
    private IMkDealerProfileService dealerProfileService;

    @Autowired
    private IMkDealerSellFlowService dealerSellFlowService;

    /**
     * 客户流失预测
     */
    @PreAuthorize("@ss.hasPermi('mk:ai:churnPrediction')")
    @GetMapping("/churnPrediction")
    public AjaxResult churnPrediction()
    {
        List<MkCustomer> customers = customerService.selectCustomerList(new MkCustomer());
        List<MkOrder> orders = orderService.selectOrderList(new MkOrder());
        long now = System.currentTimeMillis();

        // 客户 → 最近订单时间 + 订单数 + 累计金额
        Map<Long, java.util.Date> lastOrderTime = new HashMap<>();
        Map<Long, Integer> orderCount = new HashMap<>();
        Map<Long, BigDecimal> orderAmount = new HashMap<>();
        if (orders != null)
        {
            for (MkOrder o : orders)
            {
                if (o.getCustomerId() == null || o.getCreateTime() == null) continue;
                lastOrderTime.merge(o.getCustomerId(), o.getCreateTime(), (a, b) -> a.after(b) ? a : b);
                orderCount.merge(o.getCustomerId(), 1, Integer::sum);
                BigDecimal amt = o.getOrderAmount() == null ? BigDecimal.ZERO : o.getOrderAmount();
                orderAmount.merge(o.getCustomerId(), amt, BigDecimal::add);
            }
        }

        // 高价值阈值（累计金额Top20%）
        List<BigDecimal> amounts = new ArrayList<>();
        if (customers != null)
        {
            for (MkCustomer c : customers)
            {
                if (c.getCustomerId() != null && orderAmount.containsKey(c.getCustomerId()))
                {
                    amounts.add(orderAmount.get(c.getCustomerId()));
                }
            }
        }
        amounts.sort((a, b) -> b.compareTo(a));
        BigDecimal highValueThreshold = amounts.isEmpty() ? BigDecimal.ZERO
                : amounts.get(Math.max(0, (int) (amounts.size() * 0.2) - 1));

        // P2-03 经销商维度数据：档案（层级/授权区域/未结返利）+ 流向最近登记时间
        Map<Long, MkDealerProfile> profileMap = new HashMap<>();
        List<MkDealerProfile> profiles = dealerProfileService.selectDealerProfileList(new MkDealerProfile());
        if (profiles != null)
        {
            for (MkDealerProfile p : profiles)
            {
                if (p.getCustomerId() != null) profileMap.put(p.getCustomerId(), p);
            }
        }
        Map<Long, java.util.Date> lastFlowTime = new HashMap<>();
        List<MkDealerSellFlow> flows = dealerSellFlowService.selectFlowList(new MkDealerSellFlow());
        if (flows != null)
        {
            for (MkDealerSellFlow f : flows)
            {
                if (f.getDealerId() == null || f.getFlowDate() == null) continue;
                lastFlowTime.merge(f.getDealerId(), f.getFlowDate(), (a, b) -> a.after(b) ? a : b);
            }
        }

        // 规则打分
        List<Map<String, Object>> risks = new ArrayList<>();
        if (customers != null)
        {
            for (MkCustomer c : customers)
            {
                if (c.getCustomerId() == null) continue;
                // 非成交状态客户跳过（仅评估合作中/已成交客户）
                java.util.Date lastOrder = lastOrderTime.get(c.getCustomerId());
                Integer cnt = orderCount.getOrDefault(c.getCustomerId(), 0);
                BigDecimal amt = orderAmount.getOrDefault(c.getCustomerId(), BigDecimal.ZERO);

                int risk = 0;
                boolean hasCoop = c.getCooperationDate() != null;
                if (hasCoop)
                {
                    if (lastOrder == null || (now - lastOrder.getTime()) / 86400000L > 180) risk += 40;
                    else if ((now - lastOrder.getTime()) / 86400000L > 90) risk += 20;
                    if (cnt == 0) risk += 15;
                    if ((now - c.getCooperationDate().getTime()) / (86400000L * 365) >= 1 && cnt <= 1) risk += 10;
                    if (amt.compareTo(highValueThreshold) >= 0 && amt.compareTo(BigDecimal.ZERO) > 0
                            && (lastOrder == null || (now - lastOrder.getTime()) / 86400000L > 90)) risk += 15;
                }
                // P2-03 经销商维度附加规则
                boolean isDealer = "2".equals(c.getCustomerType());
                MkDealerProfile profile = isDealer ? profileMap.get(c.getCustomerId()) : null;
                java.util.Date lastFlow = null;
                BigDecimal unsettledRebate = BigDecimal.ZERO;
                String dealerLevel = null;
                String authRegions = null;
                if (isDealer)
                {
                    if (profile != null)
                    {
                        dealerLevel = profile.getDealerLevel();
                        authRegions = profile.getAuthRegions();
                        unsettledRebate = profile.getUnsettledRebate() == null ? BigDecimal.ZERO : profile.getUnsettledRebate();
                        if (hasCoop && unsettledRebate.compareTo(BigDecimal.ZERO) > 0) risk += 10;
                    }
                    lastFlow = lastFlowTime.get(c.getCustomerId());
                    if (hasCoop && (lastFlow == null || (now - lastFlow.getTime()) / 86400000L > 90)) risk += 10;
                    if (hasCoop && "1".equals(c.getCreditStatus())) risk += 15;
                }
                if (risk >= 40)
                {
                    Map<String, Object> item = new HashMap<>();
                    item.put("customerName", c.getCustomerName());
                    item.put("customerLevel", c.getCustomerLevel());
                    item.put("customerType", c.getCustomerType());
                    item.put("dealerLevel", dealerLevel);
                    item.put("authRegions", authRegions);
                    item.put("unsettledRebate", unsettledRebate);
                    item.put("lastFlowDate", lastFlow);
                    item.put("creditStatus", c.getCreditStatus());
                    item.put("riskScore", risk);
                    item.put("riskLevel", risk >= 70 ? "高" : "中");
                    item.put("lastOrderTime", lastOrder);
                    item.put("orderCount", cnt);
                    item.put("totalAmount", amt);
                    risks.add(item);
                }
            }
        }
        risks.sort((a, b) -> Integer.compare((int) b.get("riskScore"), (int) a.get("riskScore")));

        // LLM 总结
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < risks.size() && i < 15; i++)
        {
            Map<String, Object> r = risks.get(i);
            data.append((i + 1)).append(". ").append(r.get("customerName"))
                .append("（等级").append(StringUtils.nvl(String.valueOf(r.get("customerLevel")), "-"))
                .append("）风险").append(r.get("riskScore")).append("分/").append(r.get("riskLevel"))
                .append("，订单").append(r.get("orderCount")).append("单，累计")
                .append(((BigDecimal) r.get("totalAmount")).toPlainString()).append("元");
            if ("2".equals(r.get("customerType")))
            {
                data.append("；【经销商】层级").append(StringUtils.nvl(String.valueOf(r.get("dealerLevel")), "-"))
                    .append("，授权区域").append(StringUtils.nvl(String.valueOf(r.get("authRegions")), "-"))
                    .append("，未结返利").append(((BigDecimal) r.get("unsettledRebate")).toPlainString()).append("元")
                    .append("，最近流向").append(r.get("lastFlowDate") == null ? "从未登记" : String.valueOf(r.get("lastFlowDate")))
                    .append("，信用").append("1".equals(r.get("creditStatus")) ? "冻结" : "正常");
            }
            data.append("\n");
        }
        String prompt = "你是销售管理预测助手。以下是规则计算的客户流失风险清单（按风险分降序，≥40分入列；经销商客户含层级/授权区域/未结返利/流向/信用等附加维度）。"
                + "请生成流失预警报告：1.风险概况 2.重点挽回客户（点名高价值高风险客户与未结返利较高的经销商）3.挽回策略建议（经销商侧重返利清结与流向督导）。200字内；"
                + "清单为空时生成健康度确认报告。\n\n"
                + "【风险清单】\n" + (risks.isEmpty() ? "（无高风险客户）" : data);

        AiResult ai = aiClient.chatCached("mk", "churn_prediction", prompt);
        Map<String, Object> result = new HashMap<>();
        result.put("riskCount", risks.size());
        result.put("riskList", risks);
        result.put("success", ai.isSuccess());
        result.put("aiSummary", ai.isSuccess() ? ai.getContent() : ai.getError());
        result.put("cached", ai.isCached());
        result.put("latencyMs", ai.getLatencyMs());
        return AjaxResult.success(result);
    }
}
