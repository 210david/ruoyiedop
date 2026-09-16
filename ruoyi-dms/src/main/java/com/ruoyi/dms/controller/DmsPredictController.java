package com.ruoyi.dms.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.service.IDmsEquipmentService;
import com.ruoyi.dms.service.IDmsWorkOrderService;

/**
 * DMS 设备健康与预测 AI Controller（外挂式，不改动既有业务代码）
 * P2功能：设备健康度评分+故障预测（规则计算+LLM解读）、备件消耗预测
 *
 * 健康度模型（0-100，纯规则可解释）：
 *   90天故障次数（-8分/次）+ 近30天故障次数（-10分/次）+ 累计停机时长（-1分/小时，上限20）
 *   + 维修费用负担（-1分/千元，上限10）+ 役龄（超8年-5分）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/dms/ai")
public class DmsPredictController extends BaseController
{
    @Autowired
    private AiClient aiClient;

    @Autowired
    private IDmsEquipmentService equipmentService;

    @Autowired
    private IDmsWorkOrderService workOrderService;

    /**
     * 设备健康度评分+故障预测（单台）
     */
    @PreAuthorize("@ss.hasPermi('dms:equipment:query')")
    @GetMapping("/equipmentHealth/{equipmentId}")
    public AjaxResult equipmentHealth(@PathVariable Long equipmentId)
    {
        DmsEquipment equipment = equipmentService.selectEquipmentById(equipmentId);
        if (equipment == null)
        {
            return AjaxResult.error("设备不存在");
        }

        DmsWorkOrder query = new DmsWorkOrder();
        query.setEquipmentId(equipmentId);
        List<DmsWorkOrder> orders = workOrderService.selectWorkOrderList(query);

        // 规则计算健康度
        long now = System.currentTimeMillis();
        int fault90 = 0, fault30 = 0;
        BigDecimal downtimeHours = BigDecimal.ZERO, repairCost = BigDecimal.ZERO;
        if (orders != null)
        {
            for (DmsWorkOrder wo : orders)
            {
                if (wo.getCreateTime() == null) continue;
                long ageDays = (now - wo.getCreateTime().getTime()) / 86400000L;
                if (ageDays <= 90)
                {
                    fault90++;
                    if (ageDays <= 30) fault30++;
                }
                if (wo.getDowntimeDuration() != null) downtimeHours = downtimeHours.add(wo.getDowntimeDuration());
                if (wo.getRepairCost() != null) repairCost = repairCost.add(wo.getRepairCost());
            }
        }
        int score = 100;
        score -= fault90 * 8;
        score -= fault30 * 10;
        score -= Math.min(20, downtimeHours.intValue());
        score -= Math.min(10, repairCost.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN).intValue());
        if (equipment.getPurchaseDate() != null)
        {
            long ageYears = (now - equipment.getPurchaseDate().getTime()) / (86400000L * 365);
            if (ageYears >= 8) score -= 5;
        }
        score = Math.max(0, Math.min(100, score));

        Map<String, Object> stats = new HashMap<>();
        stats.put("equipmentName", equipment.getEquipmentName());
        stats.put("fault90", fault90);
        stats.put("fault30", fault30);
        stats.put("downtimeHours", downtimeHours);
        stats.put("repairCost", repairCost);
        stats.put("score", score);
        stats.put("level", score >= 85 ? "健康" : score >= 70 ? "良好" : score >= 50 ? "关注" : "高风险");

        // LLM解读：故障预测与维保建议
        String prompt = "你是设备管理预测分析助手。以下是设备健康度规则计算结果（100分制，已由系统算出，直接引用不要重算）。\n"
                + "请生成：1.健康度解读（2句话）2.未来30天故障风险预测（高/中/低+理由，基于故障频次趋势）"
                + " 3.维保建议（1-3条可操作项）。\n"
                + "要求简洁专业，共150字内。\n\n"
                + "【设备】" + equipment.getEquipmentName()
                + "（" + StringUtils.nvl(equipment.getEquipmentCode(), "") + "，等级"
                + StringUtils.nvl(equipment.getEquipmentLevel(), "-") + "）\n"
                + "【规则计算结果】健康度" + score + "分（" + stats.get("level") + "）\n"
                + "近90天故障：" + fault90 + "次；近30天故障：" + fault30 + "次\n"
                + "累计停机：" + downtimeHours.toPlainString() + "小时；累计维修费用：" + repairCost.toPlainString() + "元\n"
                + "购入日期：" + (equipment.getPurchaseDate() == null ? "未记录" : equipment.getPurchaseDate());

        AiResult ai = aiClient.chat("dms", "equipment_health", prompt);
        Map<String, Object> result = new HashMap<>(stats);
        result.put("success", ai.isSuccess());
        result.put("aiInterpretation", ai.isSuccess() ? ai.getContent() : ai.getError());
        result.put("latencyMs", ai.getLatencyMs());
        return AjaxResult.success(result);
    }

    /**
     * 设备健康度总览（全部设备排名，取前N台风险设备）
     */
    @PreAuthorize("@ss.hasPermi('dms:equipment:list')")
    @GetMapping("/equipmentHealthOverview")
    public AjaxResult equipmentHealthOverview(@RequestParam(defaultValue = "10") int limit)
    {
        if (limit < 1 || limit > 50) limit = 10;
        List<DmsEquipment> equipments = equipmentService.selectEquipmentList(new DmsEquipment());
        List<DmsWorkOrder> allOrders = workOrderService.selectWorkOrderList(new DmsWorkOrder());

        long now = System.currentTimeMillis();
        List<Map<String, Object>> ranking = new ArrayList<>();
        if (equipments != null)
        {
            for (DmsEquipment eq : equipments)
            {
                int fault90 = 0, fault30 = 0;
                BigDecimal downtimeHours = BigDecimal.ZERO, repairCost = BigDecimal.ZERO;
                if (allOrders != null)
                {
                    for (DmsWorkOrder wo : allOrders)
                    {
                        if (!eq.getEquipmentId().equals(wo.getEquipmentId())) continue;
                        if (wo.getCreateTime() != null)
                        {
                            long ageDays = (now - wo.getCreateTime().getTime()) / 86400000L;
                            if (ageDays <= 90)
                            {
                                fault90++;
                                if (ageDays <= 30) fault30++;
                            }
                        }
                        if (wo.getDowntimeDuration() != null) downtimeHours = downtimeHours.add(wo.getDowntimeDuration());
                        if (wo.getRepairCost() != null) repairCost = repairCost.add(wo.getRepairCost());
                    }
                }
                int score = 100 - fault90 * 8 - fault30 * 10;
                score -= Math.min(20, downtimeHours.intValue());
                score -= Math.min(10, repairCost.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN).intValue());
                score = Math.max(0, Math.min(100, score));

                Map<String, Object> item = new HashMap<>();
                item.put("equipmentId", eq.getEquipmentId());
                item.put("equipmentName", eq.getEquipmentName());
                item.put("equipmentCode", eq.getEquipmentCode());
                item.put("fault90", fault90);
                item.put("fault30", fault30);
                item.put("downtimeHours", downtimeHours);
                item.put("score", score);
                item.put("level", score >= 85 ? "健康" : score >= 70 ? "良好" : score >= 50 ? "关注" : "高风险");
                ranking.add(item);
            }
        }
        ranking.sort((a, b) -> Integer.compare((int) a.get("score"), (int) b.get("score")));
        if (ranking.size() > limit) ranking = new ArrayList<>(ranking.subList(0, limit));

        // LLM 总体解读
        StringBuilder data = new StringBuilder();
        for (Map<String, Object> item : ranking)
        {
            data.append(item.get("equipmentName")).append("：健康度").append(item.get("score"))
                .append("分（").append(item.get("level")).append("），近90天故障").append(item.get("fault90"))
                .append("次，停机").append(item.get("downtimeHours")).append("小时\n");
        }
        String prompt = "你是设备管理预测分析助手。以下是设备健康度排名（由规则计算，低分在前）。"
                + "请生成设备总体健康报告：1.总体状况 2.需要重点关注的设备（点名）3.共性原因与系统性维护建议。200字内。\n\n"
                + "【健康度排名（升序，最差在前）】\n" + data;

        AiResult ai = aiClient.chat("dms", "equipment_health_overview", prompt);
        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        result.put("success", ai.isSuccess());
        result.put("aiSummary", ai.isSuccess() ? ai.getContent() : ai.getError());
        result.put("latencyMs", ai.getLatencyMs());
        return AjaxResult.success(result);
    }

    /**
     * 备件消耗预测/安全库存建议：基于库存流水计算月均消耗，对比当前库存，LLM给采购建议
     * （联动采购建议闭环：结论可直接供PMS采购参考）
     */
    @PreAuthorize("@ss.hasPermi('wms:inventory:list')")
    @GetMapping("/sparepartForecast")
    public AjaxResult sparepartForecast()
    {
        // 拉取全部库存 + 流水由前端传入材料范围太大，这里按库存快照+工单备件使用记录简化：
        // 取库存清单，LLM结合工单备件使用记录（sparePartsUsed）做趋势预测
        List<DmsWorkOrder> orders = workOrderService.selectWorkOrderList(new DmsWorkOrder());
        StringBuilder usage = new StringBuilder();
        int count = 0;
        if (orders != null)
        {
            for (DmsWorkOrder wo : orders)
            {
                String parts = wo.getSparePartsUsed();
                if (StringUtils.isNotEmpty(parts) && !"未填写".equals(parts))
                {
                    count++;
                    usage.append(wo.getEquipmentName()).append(" | ")
                         .append(wo.getCreateTime() == null ? "-" : wo.getCreateTime())
                         .append(" | 备件：").append(parts).append("\n");
                }
            }
        }

        String prompt = "你是备件管理预测助手。以下为历史维修工单的备件使用记录（" + count + "条）。"
                + "请生成备件消耗预测报告：1.高频消耗备件Top5（按出现频次） 2.消耗趋势 3.安全库存与采购建议"
                + "（含建议备货量逻辑说明）4.与采购计划联动提醒。250字内；数据不足处标注【数据不足】。\n\n"
                + "【备件使用记录】\n" + (count == 0 ? "（暂无记录）" : usage);

        AiResult ai = aiClient.chat("dms", "sparepart_forecast", prompt);
        Map<String, Object> result = new HashMap<>();
        result.put("usageCount", count);
        result.put("success", ai.isSuccess());
        result.put("content", ai.isSuccess() ? ai.getContent() : ai.getError());
        result.put("latencyMs", ai.getLatencyMs());
        return AjaxResult.success(result);
    }
}
