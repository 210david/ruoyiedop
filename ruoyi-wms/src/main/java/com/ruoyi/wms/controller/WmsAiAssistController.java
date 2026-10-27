package com.ruoyi.wms.controller;

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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsInventory;
import com.ruoyi.wms.domain.WmsStockTake;
import com.ruoyi.wms.domain.WmsStockTakeDetail;
import com.ruoyi.wms.service.IWmsInventoryService;
import com.ruoyi.wms.service.IWmsStockTakeService;

/**
 * WMS AI辅助 Controller（外挂式，不改动既有业务代码）
 * P1功能：库存预警归因分析 + 呆滞库存识别报告 + 盘点差异分析
 * AI输出均为"草稿/建议"，仅供决策参考
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/wms/ai")
public class WmsAiAssistController extends BaseController
{
    /** 呆滞判定：库龄超过N天视为呆滞候选 */
    private static final int STALE_DAYS = 90;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private IWmsInventoryService inventoryService;

    @Autowired
    private IWmsStockTakeService stockTakeService;

    /**
     * 库存预警归因分析：拉取当前库存快照，LLM分析低库存/呆滞风险并给出建议
     */
    @PreAuthorize("@ss.hasPermi('wms:ai:stockAnalysis')")
    @GetMapping("/stockAnalysis")
    public AjaxResult stockAnalysis(@RequestParam(defaultValue = "50") int limit)
    {
        if (limit < 1 || limit > 200) limit = 50;
        WmsInventory query = new WmsInventory();
        List<WmsInventory> list = inventoryService.selectInventoryList(query);
        if (list == null || list.isEmpty())
        {
            return AjaxResult.error("暂无库存数据");
        }
        // 取前limit条组装分析输入
        StringBuilder data = new StringBuilder();
        int count = 0;
        for (WmsInventory inv : list)
        {
            if (count++ >= limit) break;
            data.append(inv.getMaterialName()).append(" | 规格:").append(StringUtils.nvl(inv.getSpecModel(), "-"))
                .append(" | 仓库:").append(StringUtils.nvl(inv.getWarehouseName(), "-"))
                .append(" | 数量:").append(inv.getQty() == null ? "0" : inv.getQty().toPlainString())
                .append(" | 批次:").append(StringUtils.nvl(inv.getBatchNo(), "-"));
            if (inv.getProductionDate() != null)
            {
                data.append(" | 生产日期:").append(inv.getProductionDate());
            }
            data.append("\n");
        }

        String prompt = "你是仓储管理分析助手。根据以下库存快照数据，生成库存分析报告。\n"
                + "结构：一、库存概况 二、呆滞风险（结合生产日期推算库龄，超过" + STALE_DAYS + "天的重点标注） 三、低库存预警 四、处置建议（如促销/调拨/采购节奏）。\n"
                + "要求：结论先行，引用具体物料名称，给出可操作建议。\n\n"
                + "【库存快照（前" + count + "条）】\n" + data;

        AiResult result = aiClient.chatCached("wms", "stock_analysis", prompt);
        return AjaxResult.success(result.toMap());
    }

    /**
     * 呆滞库存识别：按库龄筛选呆滞候选清单（规则计算，不用AI，快速准确）
     */
    @PreAuthorize("@ss.hasPermi('wms:inventory:list')")
    @GetMapping("/staleInventory")
    public AjaxResult staleInventory()
    {
        WmsInventory query = new WmsInventory();
        List<WmsInventory> list = inventoryService.selectInventoryList(query);
        List<Map<String, Object>> stale = new ArrayList<>();
        long now = System.currentTimeMillis();
        if (list != null)
        {
            for (WmsInventory inv : list)
            {
                if (inv.getProductionDate() == null || inv.getQty() == null
                        || inv.getQty().compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                long ageDays = (now - inv.getProductionDate().getTime()) / 86400000L;
                if (ageDays >= STALE_DAYS)
                {
                    Map<String, Object> item = new HashMap<>();
                    item.put("materialName", inv.getMaterialName());
                    item.put("specModel", inv.getSpecModel());
                    item.put("warehouseName", inv.getWarehouseName());
                    item.put("batchNo", inv.getBatchNo());
                    item.put("qty", inv.getQty());
                    item.put("productionDate", inv.getProductionDate());
                    item.put("ageDays", ageDays);
                    stale.add(item);
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("staleCount", stale.size());
        result.put("staleList", stale);
        result.put("staleDays", STALE_DAYS);
        return AjaxResult.success(result);
    }

    /**
     * 盘点差异分析：拉取盘点单差异明细，LLM生成差异归因与处理建议
     */
    @PreAuthorize("@ss.hasPermi('wms:ai:stockTakeAnalysis')")
    @GetMapping("/stockTakeAnalysis/{takeId}")
    public AjaxResult stockTakeAnalysis(@PathVariable Long takeId)
    {
        WmsStockTake take = stockTakeService.selectStockTakeById(takeId);
        if (take == null)
        {
            return AjaxResult.error("盘点单不存在");
        }
        List<WmsStockTakeDetail> details = take.getDetailList();
        if (details == null || details.isEmpty())
        {
            return AjaxResult.error("该盘点单无明细数据");
        }

        StringBuilder diffData = new StringBuilder();
        int diffCount = 0;
        BigDecimal totalDiff = BigDecimal.ZERO;
        for (WmsStockTakeDetail d : details)
        {
            if (d.getDiffQty() != null && d.getDiffQty().compareTo(BigDecimal.ZERO) != 0)
            {
                diffCount++;
                totalDiff = totalDiff.add(d.getDiffQty());
                diffData.append(d.getMaterialName()).append(" | 库位:").append(StringUtils.nvl(d.getLocationCode(), "-"))
                        .append(" | 账面:").append(d.getBookQty())
                        .append(" | 实盘:").append(d.getActualQty())
                        .append(" | 差异:").append(d.getDiffQty()).append("\n");
            }
        }

        String prompt;
        if (diffCount == 0)
        {
            prompt = "你是仓储管理分析助手。该盘点单（" + StringUtils.nvl(take.getTakeNo(), "") + "，仓库："
                    + StringUtils.nvl(take.getWarehouseName(), "") + "）全部物料账实相符，无差异。请生成一份简短的盘点结论报告（100字内），肯定账实一致性并给出管理建议。";
        }
        else
        {
            prompt = "你是仓储盘点分析助手。根据以下盘点差异明细，生成差异分析报告。\n"
                    + "结构：一、差异概况（共" + diffCount + "项差异，累计差异数量" + totalDiff.toPlainString() + "）"
                    + " 二、差异归因分析（常见原因：收发计量误差/漏记账/串库/损耗/单据延迟，结合物料与差异方向判断）"
                    + " 三、处理建议（复盘重点物料/流程改进/账务调整建议，账务调整需按公司流程审批）。\n\n"
                    + "【盘点差异明细】\n" + diffData;
        }

        AiResult result = aiClient.chatCached("wms", "stocktake_analysis", prompt);
        return AjaxResult.success(result.toMap());
    }
}
