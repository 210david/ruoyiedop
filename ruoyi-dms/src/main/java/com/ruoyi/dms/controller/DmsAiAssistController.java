package com.ruoyi.dms.controller;

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
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.service.IDmsWorkOrderService;

/**
 * DMS AI辅助补充 Controller（外挂式新增，不改动既有业务代码）
 * P0功能：维修完工报告自动生成（工单数据 → 报告草稿填入完工表单，人工确认后保存）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/dms/ai")
public class DmsAiAssistController extends BaseController
{
    @Autowired
    private AiClient aiClient;

    @Autowired
    private IDmsWorkOrderService workOrderService;

    /**
     * 完工报告草稿生成：基于工单全量数据（故障、过程、备件、耗时）生成完工说明草稿
     */
    @PreAuthorize("@ss.hasPermi('dms:workorder:query')")
    @GetMapping("/completeReport/{orderId}")
    public AjaxResult completeReport(@PathVariable Long orderId)
    {
        DmsWorkOrder wo = workOrderService.selectWorkOrderById(orderId);
        if (wo == null)
        {
            return AjaxResult.error("工单不存在");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是设备维修完工报告撰写助手。根据以下工单数据，生成一段完工说明草稿（200字内）。\n")
              .append("要求：概述故障现象→处理过程→更换备件→结果验证→设备状态结论；专业简洁，事实以工单数据为准，")
              .append("数据缺失处不要编造，写【待补充】。\n\n")
              .append("【工单数据】\n")
              .append("工单号：").append(StringUtils.nvl(wo.getOrderNo(), "")).append("\n")
              .append("设备名称：").append(StringUtils.nvl(wo.getEquipmentName(), "")).append("\n")
              .append("故障现象：").append(StringUtils.nvl(wo.getFaultDescription(), "未填写")).append("\n")
              .append("故障原因：").append(StringUtils.nvl(wo.getFaultCause(), "未填写")).append("\n")
              .append("维修措施：").append(StringUtils.nvl(wo.getRepairMeasure(), "未填写")).append("\n")
              .append("更换备件：").append(StringUtils.nvl(wo.getSparePartsUsed(), "未填写")).append("\n")
              .append("维修费用(元)：").append(wo.getRepairCost() == null ? "" : wo.getRepairCost().toPlainString()).append("\n")
              .append("停机时长(小时)：").append(wo.getDowntimeDuration() == null ? "" : wo.getDowntimeDuration().toPlainString()).append("\n");

        AiResult result = aiClient.chat("dms", "complete_report", prompt.toString());
        Map<String, Object> data = new HashMap<>(result.toMap());
        if (result.isSuccess())
        {
            data.put("draft", result.getContent());
        }
        return AjaxResult.success(data);
    }
}
