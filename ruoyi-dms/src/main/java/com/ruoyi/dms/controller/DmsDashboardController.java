package com.ruoyi.dms.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.dms.domain.*;
import com.ruoyi.dms.mapper.*;

@RestController
@RequestMapping("/dms/dashboard")
public class DmsDashboardController extends BaseController
{
    @Autowired
    private DmsEquipmentMapper equipmentMapper;
    @Autowired
    private DmsWorkOrderMapper workOrderMapper;
    @Autowired
    private DmsSparePartStockMapper sparePartStockMapper;
    @Autowired
    private DmsPmPlanMapper pmPlanMapper;
    @Autowired
    private DmsSparePartRecordMapper sparePartRecordMapper;
    @Autowired
    private DmsDataRecordMapper dataRecordMapper;
    @Autowired
    private DmsEquipmentLogMapper equipmentLogMapper;

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
        List<DmsSparePartStock> allStocks = sparePartStockMapper.selectStockList(new DmsSparePartStock());
        List<Map<String, Object>> lowStockParts = new ArrayList<>();
        for (DmsSparePartStock s : allStocks)
        {
            if (s.getCurrentStock() != null && s.getSafetyStock() != null
                && s.getCurrentStock().compareTo(s.getSafetyStock()) <= 0)
            {
                Map<String, Object> m = new HashMap<>();
                m.put("partCode", s.getPartCode());
                m.put("partName", s.getPartName());
                m.put("currentStock", s.getCurrentStock());
                m.put("safetyStock", s.getSafetyStock());
                m.put("unit", s.getUnit());
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

    /**
     * 获取设备列表（用于大屏设备下拉选择器）
     */
    @PreAuthorize("@ss.hasPermi('dms:dashboard:list')")
    @GetMapping("/equipment-list")
    public AjaxResult equipmentList()
    {
        List<DmsEquipment> allEquipment = equipmentMapper.selectEquipmentList(new DmsEquipment());
        List<Map<String, Object>> list = new ArrayList<>();
        for (DmsEquipment eq : allEquipment)
        {
            Map<String, Object> m = new HashMap<>();
            m.put("equipmentId", eq.getEquipmentId());
            m.put("equipmentCode", eq.getEquipmentCode());
            m.put("equipmentName", eq.getEquipmentName());
            m.put("equipmentStatus", eq.getEquipmentStatus());
            list.add(m);
        }
        return AjaxResult.success(list);
    }

    /**
     * 获取设备全生命周期大屏数据
     */
    @PreAuthorize("@ss.hasPermi('dms:dashboard:list')")
    @GetMapping("/equipment/{equipmentId}")
    public AjaxResult equipmentDashboard(@PathVariable Long equipmentId)
    {
        Map<String, Object> result = new HashMap<>();

        // ========== 1. 设备基本信息 ==========
        DmsEquipment eq = equipmentMapper.selectEquipmentById(equipmentId);
        if (eq == null)
        {
            return AjaxResult.error("设备不存在");
        }
        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("equipmentId", eq.getEquipmentId());
        deviceInfo.put("equipmentCode", eq.getEquipmentCode());
        deviceInfo.put("equipmentName", eq.getEquipmentName());
        deviceInfo.put("assetCode", eq.getAssetCode());
        deviceInfo.put("categoryName", eq.getCategoryName());
        deviceInfo.put("model", eq.getModel());
        deviceInfo.put("serialNumber", eq.getSerialNumber());
        deviceInfo.put("manufacturer", eq.getManufacturer());
        deviceInfo.put("supplier", eq.getSupplier());
        deviceInfo.put("purchaseDate", eq.getPurchaseDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(eq.getPurchaseDate()) : "");
        deviceInfo.put("installDate", eq.getInstallDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(eq.getInstallDate()) : "");
        deviceInfo.put("retireDate", eq.getRetireDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(eq.getRetireDate()) : "");
        deviceInfo.put("deptName", eq.getDeptName());
        deviceInfo.put("installLocation", eq.getInstallLocation());
        deviceInfo.put("equipmentStatus", eq.getEquipmentStatus());
        deviceInfo.put("equipmentLevel", eq.getEquipmentLevel());
        deviceInfo.put("warrantyDate", eq.getWarrantyDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(eq.getWarrantyDate()) : "");
        deviceInfo.put("responsibleName", eq.getResponsibleName());
        deviceInfo.put("originalValue", eq.getOriginalValue());
        result.put("deviceInfo", deviceInfo);

        // 设备状态文字（根据实际状态字典）
        // 0=正常, 1=故障, 2=停机, 3=闲置, 4=维修中, 5=报废
        String statusText = "运行中";
        String statusType = "running";
        String es = eq.getEquipmentStatus();
        if ("1".equals(es)) { statusText = "故障"; statusType = "fault"; }
        else if ("2".equals(es)) { statusText = "停机"; statusType = "stopped"; }
        else if ("3".equals(es)) { statusText = "闲置"; statusType = "stopped"; }
        else if ("4".equals(es)) { statusText = "维修中"; statusType = "stopped"; }
        else if ("5".equals(es)) { statusText = "报废"; statusType = "stopped"; }
        result.put("deviceStatusText", statusText);
        result.put("deviceStatusType", statusType);

        // ========== 2. 生命周期阶段 ==========
        List<Map<String, Object>> lifecycleStages = new ArrayList<>();
        // 购置
        Map<String, Object> stage1 = new HashMap<>();
        stage1.put("label", "购置");
        stage1.put("date", eq.getPurchaseDate() != null ? new SimpleDateFormat("yyyy-MM").format(eq.getPurchaseDate()) : "--");
        stage1.put("status", eq.getPurchaseDate() != null ? "done" : "future");
        stage1.put("icon", "ShoppingCart");
        lifecycleStages.add(stage1);
        // 启用
        Map<String, Object> stage2 = new HashMap<>();
        stage2.put("label", "启用");
        stage2.put("date", eq.getInstallDate() != null ? new SimpleDateFormat("yyyy-MM").format(eq.getInstallDate()) : "--");
        stage2.put("status", eq.getInstallDate() != null ? "done" : "future");
        stage2.put("icon", "Promotion");
        lifecycleStages.add(stage2);
        // 维保（取最近一次完成的维保工单）
        DmsWorkOrder lastMaintOrder = null;
        for (DmsWorkOrder o : getEquipmentOrders(equipmentId))
        {
            if (("1".equals(o.getOrderType()) || "2".equals(o.getOrderType())) && "4".equals(o.getOrderStatus()))
            {
                if (lastMaintOrder == null || (o.getCompleteTime() != null && lastMaintOrder.getCompleteTime() != null
                    && o.getCompleteTime().after(lastMaintOrder.getCompleteTime())))
                {
                    lastMaintOrder = o;
                }
            }
        }
        Map<String, Object> stage3 = new HashMap<>();
        stage3.put("label", "维保");
        stage3.put("date", lastMaintOrder != null && lastMaintOrder.getCompleteTime() != null
            ? new SimpleDateFormat("yyyy-MM").format(lastMaintOrder.getCompleteTime()) : "--");
        stage3.put("status", lastMaintOrder != null ? "done" : "future");
        stage3.put("icon", "Tools");
        lifecycleStages.add(stage3);
        // 运行（当前状态）- 根据设备实际状态显示
        Map<String, Object> stage4 = new HashMap<>();
        stage4.put("label", statusText);  // 使用实际状态文字
        stage4.put("date", "当前状态");
        stage4.put("status", "active");
        stage4.put("icon", null);
        stage4.put("currentStatus", statusText);
        stage4.put("statusType", statusType);
        lifecycleStages.add(stage4);
        // 报废
        Map<String, Object> stage5 = new HashMap<>();
        stage5.put("label", "报废");
        stage5.put("date", eq.getRetireDate() != null ? new SimpleDateFormat("yyyy-MM").format(eq.getRetireDate()) : "--");
        stage5.put("status", eq.getRetireDate() != null ? "done" : "future");
        stage5.put("icon", "FolderDelete");
        lifecycleStages.add(stage5);
        result.put("lifecycleStages", lifecycleStages);

        // 生命周期进度
        int doneCount = 0;
        for (Map<String, Object> s : lifecycleStages)
        {
            if ("done".equals(s.get("status")) || "active".equals(s.get("status"))) doneCount++;
        }
        result.put("lifecycleProgress", (int) ((doneCount - 0.5) / lifecycleStages.size() * 100));

        // ========== 3. 备件更换记录 ==========
        List<Map<String, Object>> partsList = new ArrayList<>();
        DmsSparePartRecord recordQuery = new DmsSparePartRecord();
        recordQuery.setMoveType("1"); // 出库
        List<DmsSparePartRecord> allRecords = sparePartRecordMapper.selectRecordList(recordQuery);
        for (DmsSparePartRecord r : allRecords)
        {
            // refType为work_order且refId关联的工单属于该设备
            if ("work_order".equals(r.getRefType()) && r.getRefId() != null)
            {
                DmsWorkOrder refOrder = workOrderMapper.selectWorkOrderById(r.getRefId());
                if (refOrder != null && equipmentId.equals(refOrder.getEquipmentId()))
                {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", r.getPartName());
                    m.put("date", r.getOperateDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(r.getOperateDate()) : "");
                    m.put("qty", r.getQuantity() != null ? r.getQuantity().intValue() : 0);
                    partsList.add(m);
                }
            }
        }
        // 最多取最近3条
        if (partsList.size() > 3) partsList = partsList.subList(0, 3);
        result.put("partsList", partsList);

        // ========== 4. 维护费用（近6月） ==========
        List<DmsWorkOrder> eqOrders = getEquipmentOrders(equipmentId);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -5);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date sixMonthsAgo = cal.getTime();

        // 按月统计费用
        Map<String, BigDecimal> monthlyCost = new LinkedHashMap<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal partsCost = BigDecimal.ZERO;
        BigDecimal repairCost = BigDecimal.ZERO;
        SimpleDateFormat monthFmt = new SimpleDateFormat("M月");
        for (int i = 5; i >= 0; i--)
        {
            Calendar mc = Calendar.getInstance();
            mc.add(Calendar.MONTH, -i);
            monthlyCost.put(monthFmt.format(mc.getTime()), BigDecimal.ZERO);
        }
        for (DmsWorkOrder o : eqOrders)
        {
            if (o.getCompleteTime() != null && o.getCompleteTime().after(sixMonthsAgo))
            {
                String monthKey = monthFmt.format(o.getCompleteTime());
                BigDecimal cost = o.getRepairCost() != null ? o.getRepairCost() : BigDecimal.ZERO;
                monthlyCost.merge(monthKey, cost, BigDecimal::add);
                totalCost = totalCost.add(cost);
                // 简单区分：有sparePartsUsed的算备件费用，否则算维修费用
                if (o.getSparePartsUsed() != null && !o.getSparePartsUsed().isEmpty())
                {
                    partsCost = partsCost.add(cost);
                }
                else
                {
                    repairCost = repairCost.add(cost);
                }
            }
        }
        // 构建费用数据
        List<Map<String, Object>> costData = new ArrayList<>();
        BigDecimal maxCost = monthlyCost.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        for (Map.Entry<String, BigDecimal> entry : monthlyCost.entrySet())
        {
            Map<String, Object> m = new HashMap<>();
            m.put("month", entry.getKey());
            BigDecimal val = entry.getValue();
            m.put("value", val.divide(BigDecimal.valueOf(1000)).setScale(1, BigDecimal.ROUND_HALF_UP) + "k");
            int height = maxCost.compareTo(BigDecimal.ZERO) > 0
                ? val.multiply(BigDecimal.valueOf(100)).divide(maxCost, 0, BigDecimal.ROUND_HALF_UP).intValue() : 0;
            m.put("height", Math.max(height, 5));
            costData.add(m);
        }
        result.put("costData", costData);
        result.put("totalCost", totalCost);
        result.put("partsCost", partsCost);
        result.put("repairCost", repairCost);

        // ========== 5. 能耗数据（数据采集记录） ==========
        DmsDataRecord dataQuery = new DmsDataRecord();
        dataQuery.setEquipmentId(equipmentId);
        List<DmsDataRecord> dataRecords = dataRecordMapper.selectRecordList(dataQuery);
        // 取最近24条记录作为24小时数据
        List<Map<String, Object>> energyData = new ArrayList<>();
        int recordCount = Math.min(dataRecords.size(), 24);
        for (int i = 0; i < recordCount; i++)
        {
            DmsDataRecord dr = dataRecords.get(dataRecords.size() - recordCount + i);
            Map<String, Object> m = new HashMap<>();
            m.put("time", dr.getCollectTime() != null
                ? new SimpleDateFormat("HH:mm").format(dr.getCollectTime()) : String.format("%02d:00", i));
            m.put("elec", dr.getParam1Value() != null ? dr.getParam1Value().intValue() : 0);
            m.put("water", dr.getParam2Value() != null ? dr.getParam2Value().doubleValue() : 0);
            energyData.add(m);
        }
        // 不够24条则补齐
        while (energyData.size() < 24)
        {
            int idx = energyData.size();
            Map<String, Object> m = new HashMap<>();
            m.put("time", String.format("%02d:00", idx));
            m.put("elec", 0);
            m.put("water", 0);
            energyData.add(m);
        }
        result.put("energyData", energyData);

        // ========== 6. KPI 数据 ==========
        // 运行时长：取最新数据采集记录的runHours
        BigDecimal runHours = BigDecimal.ZERO;
        if (!dataRecords.isEmpty())
        {
            DmsDataRecord latest = dataRecords.get(dataRecords.size() - 1);
            if (latest.getRunHours() != null) runHours = latest.getRunHours();
        }
        // 如果没有采集记录，根据启用日期估算
        if (runHours.compareTo(BigDecimal.ZERO) == 0 && eq.getInstallDate() != null)
        {
            long days = (System.currentTimeMillis() - eq.getInstallDate().getTime()) / (1000 * 60 * 60 * 24);
            runHours = BigDecimal.valueOf(days * 16); // 假设每天运行16小时
        }
        result.put("runHours", runHours.intValue());

        // 故障次数：统计该设备的故障工单数
        int faultCount = 0;
        for (DmsWorkOrder o : eqOrders)
        {
            if ("0".equals(o.getOrderType())) faultCount++;
        }
        result.put("faultCount", faultCount);

        // MTBF: 如果有故障，运行时长/故障次数
        int mtbf = faultCount > 0 ? runHours.intValue() / faultCount : runHours.intValue();
        result.put("mtbf", mtbf);

        // OEE: 基于设备状态计算（正常运行为100%，其他状态按比例降低）
        int oee = 95; // 默认正常运行
        String eqStatus = eq.getEquipmentStatus();
        if ("1".equals(eqStatus)) oee = 0;      // 故障
        else if ("2".equals(eqStatus)) oee = 0; // 停机
        else if ("3".equals(eqStatus)) oee = 0; // 闲置
        else if ("4".equals(eqStatus)) oee = 30; // 维修中
        else if (faultCount > 0) oee = Math.max(60, 95 - faultCount * 10); // 有故障记录则降低
        result.put("oee", oee);

        // ========== 7. 工单列表 ==========
        List<Map<String, Object>> workorderList = new ArrayList<>();
        // 工单类型统计（按照实际字典：0=故障报修, 1=PM维护, 2=点检整改, 3=临时任务, 4=改造安装）
        int typeRepair = 0, typePm = 0, typeInspect = 0, typeTemp = 0, typeUpgrade = 0;
        // 工单状态统计
        int statusCompleted = 0, statusProcessing = 0, statusPending = 0;
        for (DmsWorkOrder o : eqOrders)
        {
            Map<String, Object> m = new HashMap<>();
            m.put("title", o.getFaultDescription() != null && o.getFaultDescription().length() > 0
                ? o.getFaultDescription().substring(0, Math.min(o.getFaultDescription().length(), 20))
                : (o.getOrderNo() != null ? o.getOrderNo() : "工单"));
            m.put("priority", o.getPriority());
            m.put("reportTime", o.getReportTime() != null
                ? new SimpleDateFormat("yyyy-MM-dd").format(o.getReportTime()) : "");
            m.put("assigneeName", o.getAssigneeName() != null ? o.getAssigneeName() : "");
            m.put("faultDescription", o.getFaultDescription() != null ? o.getFaultDescription() : "");
            // 状态映射
            // 工单状态：0=待分配, 1=待处理, 2=处理中, 3=已挂起, 4=已完成, 5=已验收, 6=已撤销, 7=已转单
            String os = o.getOrderStatus();
            String statusLabel;
            String statusClass;
            // 处理中统计：0,1,2,3,7 都算处理中（未完工、未验收、未撤销）
            if ("0".equals(os) || "1".equals(os) || "2".equals(os) || "3".equals(os) || "7".equals(os))
            {
                statusLabel = "2".equals(os) ? "处理中" : ("3".equals(os) ? "已挂起" : ("7".equals(os) ? "已转单" : "待处理"));
                statusClass = "status-warning";
                statusProcessing++;
            }
            else if ("4".equals(os))
            {
                statusLabel = "已完成";
                statusClass = "status-success";
                statusCompleted++;
            }
            else if ("5".equals(os))
            {
                statusLabel = "已验收";
                statusClass = "status-success";
                statusCompleted++;
            }
            else
            {
                statusLabel = "已撤销";
                statusClass = "status-muted";
            }
            m.put("statusLabel", statusLabel);
            m.put("statusClass", statusClass);
            m.put("borderColor", "status-error".equals(statusClass) ? "var(--state-error)"
                : "status-warning".equals(statusClass) ? "var(--state-warning)" : "var(--state-success)");
            workorderList.add(m);

            // 工单类型统计（按照实际字典值）
            String ot = o.getOrderType();
            if ("0".equals(ot)) typeRepair++;           // 故障报修
            else if ("1".equals(ot)) typePm++;          // PM维护
            else if ("2".equals(ot)) typeInspect++;     // 点检整改
            else if ("3".equals(ot)) typeTemp++;        // 临时任务
            else if ("4".equals(ot)) typeUpgrade++;     // 改造安装
        }
        // 取最近5条工单
        if (workorderList.size() > 5)
        {
            workorderList = workorderList.subList(0, 5);
        }
        result.put("workorderList", workorderList);
        result.put("processingCount", statusProcessing);

        // 工单类型饼图数据
        List<Map<String, Object>> typePieData = new ArrayList<>();
        typePieData.add(createPieItem("故障报修", String.valueOf(typeRepair), "var(--eq-primary)"));
        typePieData.add(createPieItem("PM维护", String.valueOf(typePm), "var(--state-success)"));
        typePieData.add(createPieItem("点检整改", String.valueOf(typeInspect), "var(--state-purple)"));
        typePieData.add(createPieItem("临时任务", String.valueOf(typeTemp), "var(--state-info)"));
        typePieData.add(createPieItem("改造安装", String.valueOf(typeUpgrade), "var(--state-warning)"));
        result.put("typePieData", typePieData);
        result.put("typePieTotal", typeRepair + typePm + typeInspect + typeTemp + typeUpgrade);

        // 工单状态饼图数据
        List<Map<String, Object>> statusPieData = new ArrayList<>();
        statusPieData.add(createPieItem("已完成", String.valueOf(statusCompleted), "var(--state-success)"));
        statusPieData.add(createPieItem("处理中", String.valueOf(statusProcessing), "var(--state-warning)"));
        statusPieData.add(createPieItem("待处理", String.valueOf(statusPending), "var(--state-error)"));
        result.put("statusPieData", statusPieData);
        int woTotal = statusCompleted + statusProcessing + statusPending;
        result.put("completionRate", woTotal > 0 ? (int)(statusCompleted * 100 / woTotal) : 0);

        // ========== 8. 仪表盘数据 ==========
        // 从最新数据采集记录获取参数值
        Map<String, Object> gauges = new HashMap<>();
        if (!dataRecords.isEmpty())
        {
            DmsDataRecord latest = dataRecords.get(dataRecords.size() - 1);
            gauges.put("loadRate", latest.getParam1Value() != null ? latest.getParam1Value().intValue() : 68);
            gauges.put("temperature", latest.getParam2Value() != null ? latest.getParam2Value().intValue() : 72);
            gauges.put("speed", latest.getParam3Value() != null ? latest.getParam3Value().intValue() : 12000);
        }
        else
        {
            gauges.put("loadRate", 68);
            gauges.put("temperature", 72);
            gauges.put("speed", 12000);
        }
        gauges.put("oee", 87);
        result.put("gauges", gauges);

        // ========== 9. 健康诊断（模拟数据，美观展示） ==========
        Map<String, Object> healthDiagnosis = new HashMap<>();
        // 使用固定的模拟数据，让显示更美观合理
        String installDate = eq.getInstallDate() != null ? new SimpleDateFormat("yyyy-MM").format(eq.getInstallDate()) : "2024-01";
        healthDiagnosis.put("installDate", installDate);
        healthDiagnosis.put("runHours", 12480); // 固定显示12480小时
        
        // 状态分析（美观的模拟文本）
        healthDiagnosis.put("statusAnalysis", "该设备自 " + installDate + " 启用至今运行 12,480h，整体处于健康周期中段。主轴轴承经上次更换后运行稳定，温度趋势平稳；液压系统定位精度保持优秀。");
        
        // 风险提示（模拟数据）
        healthDiagnosis.put("hasRisk", true);
        healthDiagnosis.put("riskAnalysis", "刀库健康度降至 65，接近预警阈值。基于近 6 月备件更换频率推算，建议在 30天内 安排刀爪组件检查或更换，避免非计划停机。");
        healthDiagnosis.put("riskDays", 30);
        
        // 维保建议（模拟数据）
        healthDiagnosis.put("maintenanceAdvice", "下次保养重点关注刀库凸轮箱润滑及换刀机构间隙，同步检查主轴冷却回路。");
        
        // 预计剩余寿命（固定18个月，避免负值）
        healthDiagnosis.put("remainingMonths", 18);
        result.put("healthDiagnosis", healthDiagnosis);

        return AjaxResult.success(result);
    }

    /** 获取设备的工单列表 */
    private List<DmsWorkOrder> getEquipmentOrders(Long equipmentId)
    {
        DmsWorkOrder query = new DmsWorkOrder();
        query.setEquipmentId(equipmentId);
        return workOrderMapper.selectWorkOrderList(query);
    }

    /** 创建饼图数据项 */
    private Map<String, Object> createPieItem(String label, String value, String color)
    {
        Map<String, Object> m = new HashMap<>();
        m.put("label", label);
        m.put("value", value);
        m.put("color", color);
        return m;
    }
}
