package com.ruoyi.dms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.domain.DmsSparePart;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.mapper.DmsPmPlanMapper;
import com.ruoyi.dms.mapper.DmsSparePartMapper;
import com.ruoyi.dms.mapper.DmsWorkOrderMapper;
import com.ruoyi.dms.domain.DmsPmPlan;

@RestController
@RequestMapping("/dms/dashboard")
public class DmsDashboardController extends BaseController
{
    @Autowired
    private DmsEquipmentMapper equipmentMapper;
    @Autowired
    private DmsWorkOrderMapper workOrderMapper;
    @Autowired
    private DmsSparePartMapper sparePartMapper;
    @Autowired
    private DmsPmPlanMapper pmPlanMapper;

    @PreAuthorize("@ss.hasPermi('dms:dashboard:list')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        Map<String, Object> result = new HashMap<>();

        // 1. 设备统计
        List<DmsEquipment> allEquipment = equipmentMapper.selectEquipmentList(new DmsEquipment());
        int eqTotal = allEquipment.size();
        int eqInUse = 0, eqIdle = 0, eqRepair = 0, eqFault = 0, eqScrap = 0;
        for (DmsEquipment eq : allEquipment)
        {
            String s = eq.getEquipmentStatus();
            if ("0".equals(s)) eqInUse++;
            else if ("1".equals(s)) eqIdle++;
            else if ("2".equals(s)) eqRepair++;
            else if ("4".equals(s)) eqFault++;
            else if ("5".equals(s)) eqScrap++;
        }
        Map<String, Object> eqStats = new HashMap<>();
        eqStats.put("total", eqTotal);
        eqStats.put("inUse", eqInUse);
        eqStats.put("idle", eqIdle);
        eqStats.put("repair", eqRepair);
        eqStats.put("fault", eqFault);
        eqStats.put("scrap", eqScrap);
        result.put("equipment", eqStats);

        // 2. 工单统计
        List<DmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(new DmsWorkOrder());
        int woTotal = allOrders.size();
        int woPending = 0, woProcessing = 0, woCompleted = 0, woVerified = 0;
        for (DmsWorkOrder o : allOrders)
        {
            String s = o.getOrderStatus();
            if ("0".equals(s) || "1".equals(s)) woPending++;
            else if ("2".equals(s) || "3".equals(s) || "7".equals(s)) woProcessing++;
            else if ("4".equals(s)) woCompleted++;
            else if ("5".equals(s)) woVerified++;
        }
        Map<String, Object> woStats = new HashMap<>();
        woStats.put("total", woTotal);
        woStats.put("pending", woPending);
        woStats.put("processing", woProcessing);
        woStats.put("completed", woCompleted);
        woStats.put("verified", woVerified);
        result.put("workOrder", woStats);

        // 3. 备件预警
        List<DmsSparePart> allParts = sparePartMapper.selectSparePartList(new DmsSparePart());
        List<Map<String, Object>> lowStockParts = new ArrayList<>();
        for (DmsSparePart p : allParts)
        {
            if (p.getCurrentStock() != null && p.getSafetyStock() != null
                && p.getCurrentStock().compareTo(p.getSafetyStock()) <= 0)
            {
                Map<String, Object> m = new HashMap<>();
                m.put("partCode", p.getPartCode());
                m.put("partName", p.getPartName());
                m.put("currentStock", p.getCurrentStock());
                m.put("safetyStock", p.getSafetyStock());
                m.put("unit", p.getUnit());
                lowStockParts.add(m);
            }
        }
        result.put("lowStockParts", lowStockParts);
        result.put("lowStockCount", lowStockParts.size());

        // 4. PM计划统计
        List<DmsPmPlan> allPlans = pmPlanMapper.selectPmPlanList(new DmsPmPlan());
        Map<String, Object> pmStats = new HashMap<>();
        pmStats.put("total", allPlans.size());
        int pmActive = 0;
        for (DmsPmPlan p : allPlans) { if ("0".equals(p.getStatus())) pmActive++; }
        pmStats.put("active", pmActive);
        result.put("pmPlan", pmStats);

        return AjaxResult.success(result);
    }
}
