package com.ruoyi.dms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.ruoyi.dms.service.IDmsAiService;
import com.ruoyi.dms.service.IDmsEquipmentService;
import com.ruoyi.dms.service.IDmsWorkOrderService;

/**
 * DMS 工单全流程 AI Agent Controller（外挂式，不改动既有业务代码）
 * P2功能：报修→诊断→备件清单→派工推荐 全链路串联，一次调用返回完整处置方案
 * 结果为"建议方案"，人工确认后按既有流程操作（不自动写库）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/dms/ai")
public class DmsWorkOrderAgentController extends BaseController
{
    @Autowired
    private AiClient aiClient;

    @Autowired
    private IDmsWorkOrderService workOrderService;

    @Autowired
    private IDmsEquipmentService equipmentService;

    @Autowired
    private IDmsAiService dmsAiService;

    /**
     * 工单全流程 Agent：诊断 → 备件清单 → 派工推荐，一次返回
     */
    @PreAuthorize("@ss.hasPermi('dms:ai:agent')")
    @GetMapping("/agent/{orderId}")
    public AjaxResult agent(@PathVariable Long orderId)
    {
        DmsWorkOrder wo = workOrderService.selectWorkOrderById(orderId);
        if (wo == null)
        {
            return AjaxResult.error("工单不存在");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", wo.getOrderNo());
        result.put("equipmentName", wo.getEquipmentName());

        // ===== Step1: 智能派工推荐（复用既有能力） =====
        Map<String, Object> dispatch = dmsAiService.dispatchRecommend(orderId);
        result.put("dispatch", dispatch);

        // ===== Step2: 设备历史上下文 =====
        StringBuilder history = new StringBuilder();
        DmsEquipment equipment = wo.getEquipmentId() == null ? null : equipmentService.selectEquipmentById(wo.getEquipmentId());
        DmsWorkOrder query = new DmsWorkOrder();
        if (wo.getEquipmentId() != null) query.setEquipmentId(wo.getEquipmentId());
        List<DmsWorkOrder> relatedOrders = workOrderService.selectWorkOrderList(query);
        int relatedCount = 0;
        if (relatedOrders != null)
        {
            for (DmsWorkOrder ro : relatedOrders)
            {
                if (ro.getOrderId().equals(orderId)) continue;
                if (relatedCount >= 5) break;
                relatedCount++;
                history.append("- ").append(ro.getCreateTime() == null ? "" : ro.getCreateTime())
                       .append(" 故障：").append(StringUtils.nvl(ro.getFaultDescription(), "-"))
                       .append("｜原因：").append(StringUtils.nvl(ro.getFaultCause(), "-"))
                       .append("｜措施：").append(StringUtils.nvl(ro.getRepairMeasure(), "-")).append("\n");
            }
        }

        // ===== Step3: 诊断 + 备件清单（一次LLM调用） =====
        String prompt = "你是设备维修方案助手。为以下工单生成完整处置方案（JSON格式）。\n"
                + "返回JSON：{\n"
                + "  \"diagnosis\": \"故障诊断分析（结合设备历史，2-3句话）\",\n"
                + "  \"possibleCauses\": [\"可能原因1\", \"可能原因2\"],\n"
                + "  \"checkSteps\": [\"排查步骤（有序）\"],\n"
                + "  \"spareparts\": [\"可能需要更换的备件清单（含规格建议）\"],\n"
                + "  \"estimatedHours\": 预计维修工时(数字),\n"
                + "  \"safetyNotes\": \"安全注意事项\"\n"
                + "}\n数据不足处写【待补充】，禁止编造。\n\n"
                + "【工单】" + wo.getOrderNo() + " 设备：" + wo.getEquipmentName()
                + "（" + (equipment == null ? "未关联设备档案" : "型号" + StringUtils.nvl(equipment.getModel(), "-")
                + "，厂商" + StringUtils.nvl(equipment.getManufacturer(), "-") + "）") + "\n"
                + "【故障现象】" + StringUtils.nvl(wo.getFaultDescription(), "未填写") + "\n"
                + "【已填故障原因】" + StringUtils.nvl(wo.getFaultCause(), "未填写") + "\n\n"
                + "【该设备历史维修记录】\n" + (relatedCount == 0 ? "（无历史记录）" : history);

        // MiMo等推理模型的思考链会消耗大量token，需给足输出空间
        AiResult ai = aiClient.chatCached("dms", "workorder_agent", prompt, 8000);

        Map<String, Object> plan = new HashMap<>();
        plan.put("success", ai.isSuccess());
        plan.put("latencyMs", ai.getLatencyMs());
        if (ai.isSuccess())
        {
            plan.put("parsed", com.ruoyi.ai.util.AiJsonParser.parse(ai.getContent()));
        }
        else
        {
            plan.put("error", ai.getError());
        }
        result.put("plan", plan);
        return AjaxResult.success(result);
    }
}
