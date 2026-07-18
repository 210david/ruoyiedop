package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.domain.DmsSparePart;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.mapper.DmsPmPlanMapper;
import com.ruoyi.dms.mapper.DmsSparePartMapper;
import com.ruoyi.dms.mapper.DmsWorkOrderMapper;
import com.ruoyi.dms.domain.DmsPmPlan;
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import com.ruoyi.wms.service.IWmsStatisticsService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 平台首页聚合数据 Controller
 *
 * 汇总仓库管理(WMS)、设备管理(DMS)及后续扩展模块的关键指标，
 * 为平台首页提供统一的概览数据。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/home")
public class HomeController extends BaseController
{
    @Autowired
    private DmsEquipmentMapper equipmentMapper;
    @Autowired
    private DmsWorkOrderMapper workOrderMapper;
    @Autowired
    private DmsSparePartMapper sparePartMapper;
    @Autowired
    private DmsPmPlanMapper pmPlanMapper;
    @Autowired
    private IWmsStatisticsService wmsStatisticsService;
    @Autowired
    private ISysUserService userService;

    /**
     * 首页聚合数据
     * 无需特定权限，登录用户即可访问
     */
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        Map<String, Object> result = new HashMap<>();

        // 1. 用户欢迎信息
        Map<String, Object> welcome = new HashMap<>();
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        welcome.put("userName", username);
        if (userId != null)
        {
            SysUser user = userService.selectUserById(userId);
            if (user != null)
            {
                welcome.put("nickName", user.getNickName());
                welcome.put("avatar", user.getAvatar());
                welcome.put("deptName", user.getDept() != null ? user.getDept().getDeptName() : "");
            }
        }
        welcome.put("today", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        welcome.put("weekday", getWeekday());
        result.put("welcome", welcome);

        // 2. 预警汇总
        Map<String, Object> alerts = buildAlertSummary();
        result.put("alerts", alerts);

        // 3. 业务模块概览
        List<Map<String, Object>> modules = buildModules();
        result.put("modules", modules);

        // 4. 待办事项
        Map<String, Object> todos = buildTodos();
        result.put("todos", todos);

        // 5. WMS出入库趋势（近7天）
        try
        {
            WmsStatisticsVO trendQuery = new WmsStatisticsVO();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            java.util.Date end = cal.getTime();
            cal.add(java.util.Calendar.DAY_OF_MONTH, -6);
            java.util.Date start = cal.getTime();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Map<String, Object> params = new HashMap<>();
            params.put("beginTime", sdf.format(start));
            params.put("endTime", sdf.format(end));
            trendQuery.setParams(params);
            List<WmsStatisticsVO> trend = wmsStatisticsService.selectInboundOutboundTrend(trendQuery);
            result.put("inboundOutboundTrend", trend);
        }
        catch (Exception e)
        {
            result.put("inboundOutboundTrend", new ArrayList<>());
        }

        return AjaxResult.success(result);
    }

    /**
     * 构建预警汇总
     */
    private Map<String, Object> buildAlertSummary()
    {
        Map<String, Object> alerts = new HashMap<>();
        int total = 0;

        // WMS 预警
        Map<String, Object> wmsAlerts = new HashMap<>();
        try
        {
            List<WmsStatisticsVO> summary = wmsStatisticsService.selectStockAlertSummary();
            int lowStock = 0, overstock = 0, nearExpiry = 0, expired = 0;
            if (summary != null)
            {
                for (WmsStatisticsVO s : summary)
                {
                    String type = s.getAlertType();
                    int cnt = s.getCount() != null ? s.getCount() : 0;
                    if ("low_stock".equals(type)) lowStock = cnt;
                    else if ("overstock".equals(type)) overstock = cnt;
                    else if ("near_expiry".equals(type)) nearExpiry = cnt;
                    else if ("expired".equals(type)) expired = cnt;
                }
            }
            wmsAlerts.put("lowStock", lowStock);
            wmsAlerts.put("overstock", overstock);
            wmsAlerts.put("nearExpiry", nearExpiry);
            wmsAlerts.put("expired", expired);
            wmsAlerts.put("total", lowStock + overstock + nearExpiry + expired);
            total += lowStock + overstock + nearExpiry + expired;
        }
        catch (Exception e)
        {
            wmsAlerts.put("total", 0);
        }
        alerts.put("wms", wmsAlerts);

        // DMS 预警
        Map<String, Object> dmsAlerts = new HashMap<>();
        try
        {
            // 备件预警（低库存 + 超上限）
            List<DmsSparePart> alertParts = sparePartMapper.selectStockAlertList(new DmsSparePart());
            int partAlertCount = alertParts != null ? alertParts.size() : 0;
            dmsAlerts.put("partAlert", partAlertCount);

            // 故障设备数
            List<DmsEquipment> allEquipment = equipmentMapper.selectEquipmentList(new DmsEquipment());
            int faultEquipment = 0;
            for (DmsEquipment eq : allEquipment)
            {
                if ("4".equals(eq.getEquipmentStatus())) faultEquipment++;
            }
            dmsAlerts.put("faultEquipment", faultEquipment);

            // 待处理工单数
            List<DmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(new DmsWorkOrder());
            int pendingWorkOrders = 0;
            for (DmsWorkOrder o : allOrders)
            {
                String s = o.getOrderStatus();
                if ("0".equals(s) || "1".equals(s)) pendingWorkOrders++;
            }
            dmsAlerts.put("pendingWorkOrder", pendingWorkOrders);

            dmsAlerts.put("total", partAlertCount + faultEquipment + pendingWorkOrders);
            total += partAlertCount + faultEquipment + pendingWorkOrders;
        }
        catch (Exception e)
        {
            dmsAlerts.put("total", 0);
        }
        alerts.put("dms", dmsAlerts);

        alerts.put("total", total);
        return alerts;
    }

    /**
     * 构建业务模块概览
     */
    private List<Map<String, Object>> buildModules()
    {
        List<Map<String, Object>> modules = new ArrayList<>();

        // ===== 仓库管理 WMS =====
        Map<String, Object> wms = new HashMap<>();
        wms.put("code", "wms");
        wms.put("name", "仓库管理");
        wms.put("icon", "shopping");
        wms.put("color", "#409eff");
        wms.put("gradient", "linear-gradient(135deg, #409eff, #36cfc9)");
        wms.put("status", "active");
        wms.put("path", "/wms/inv/list");
        wms.put("desc", "物料库存、出入库管理");
        List<Map<String, Object>> wmsMetrics = new ArrayList<>();
        try
        {
            WmsStatisticsVO overview = wmsStatisticsService.selectOverview();
            wmsMetrics.add(metric("物料总数", overview.getCount() != null ? overview.getCount() : 0, "种"));
            wmsMetrics.add(metric("库存总量", overview.getQty() != null ? overview.getQty() : 0, ""));
            wmsMetrics.add(metric("已入库", overview.getInboundQty() != null ? overview.getInboundQty() : 0, "单"));
            wmsMetrics.add(metric("已出库", overview.getOutboundQty() != null ? overview.getOutboundQty() : 0, "单"));
        }
        catch (Exception e)
        {
            wmsMetrics.add(metric("物料总数", 0, "种"));
            wmsMetrics.add(metric("库存总量", 0, ""));
            wmsMetrics.add(metric("已入库", 0, "单"));
            wmsMetrics.add(metric("已出库", 0, "单"));
        }
        wms.put("metrics", wmsMetrics);
        modules.add(wms);

        // ===== 设备管理 DMS =====
        Map<String, Object> dms = new HashMap<>();
        dms.put("code", "dms");
        dms.put("name", "设备管理");
        dms.put("icon", "cpu");
        dms.put("color", "#67c23a");
        dms.put("gradient", "linear-gradient(135deg, #67c23a, #95de64)");
        dms.put("status", "active");
        dms.put("path", "/dms/dashboard/overview");
        dms.put("desc", "设备台账、工单、备件管理");
        List<Map<String, Object>> dmsMetrics = new ArrayList<>();
        try
        {
            List<DmsEquipment> allEquipment = equipmentMapper.selectEquipmentList(new DmsEquipment());
            int eqTotal = allEquipment.size();
            int eqInUse = 0;
            for (DmsEquipment eq : allEquipment)
            {
                if ("0".equals(eq.getEquipmentStatus())) eqInUse++;
            }
            double usageRate = eqTotal > 0 ? (eqInUse * 100.0 / eqTotal) : 0;

            List<DmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(new DmsWorkOrder());
            int pendingOrders = 0;
            for (DmsWorkOrder o : allOrders)
            {
                String s = o.getOrderStatus();
                if ("0".equals(s) || "1".equals(s)) pendingOrders++;
            }

            List<DmsSparePart> alertParts = sparePartMapper.selectStockAlertList(new DmsSparePart());

            dmsMetrics.add(metric("设备总数", eqTotal, "台"));
            dmsMetrics.add(metric("在用率", String.format("%.1f", usageRate), "%"));
            dmsMetrics.add(metric("待处理工单", pendingOrders, "张"));
            dmsMetrics.add(metric("备件预警", alertParts != null ? alertParts.size() : 0, "项"));
        }
        catch (Exception e)
        {
            dmsMetrics.add(metric("设备总数", 0, "台"));
            dmsMetrics.add(metric("在用率", "0.0", "%"));
            dmsMetrics.add(metric("待处理工单", 0, "张"));
            dmsMetrics.add(metric("备件预警", 0, "项"));
        }
        dms.put("metrics", dmsMetrics);
        modules.add(dms);

        // ===== 采购管理（规划中） =====
        modules.add(plannedModule("purchase", "采购管理", "shopping-cart", "#e6a23c",
                "linear-gradient(135deg, #e6a23c, #ffa940)", "采购订单、供应商管理"));

        // ===== 销售管理（规划中） =====
        modules.add(plannedModule("sales", "销售管理", "money", "#f56c6c",
                "linear-gradient(135deg, #f56c6c, #ff7875)", "销售订单、客户管理"));

        // ===== 质量管理（规划中） =====
        modules.add(plannedModule("quality", "质量管理", "medal", "#722ed1",
                "linear-gradient(135deg, #722ed1, #9254de)", "质检、不合格品处理"));

        // ===== 安全生产管理（规划中） =====
        modules.add(plannedModule("safety", "安全生产管理", "warning", "#13c2c2",
                "linear-gradient(135deg, #13c2c2, #5cdbd3)", "安全隐患、事故管理"));

        // ===== 生产管控（规划中） =====
        modules.add(plannedModule("production", "生产管控", "set-up", "#1890ff",
                "linear-gradient(135deg, #1890ff, #69b1ff)", "生产工单、工序管理"));

        // ===== 计划排产（规划中） =====
        modules.add(plannedModule("planning", "计划排产", "date", "#52c41a",
                "linear-gradient(135deg, #52c41a, #73d13d)", "生产计划、排程管理"));

        // ===== 人力资源（规划中） =====
        modules.add(plannedModule("hr", "人力资源", "peoples", "#eb2f96",
                "linear-gradient(135deg, #eb2f96, #ff85c0)", "员工、考勤、薪酬管理"));

        return modules;
    }

    /**
     * 构建待办事项
     */
    private Map<String, Object> buildTodos()
    {
        Map<String, Object> todos = new HashMap<>();
        try
        {
            List<DmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(new DmsWorkOrder());
            int pending = 0;
            for (DmsWorkOrder o : allOrders)
            {
                String s = o.getOrderStatus();
                if ("0".equals(s) || "1".equals(s)) pending++;
            }
            todos.put("workOrders", pending);
        }
        catch (Exception e)
        {
            todos.put("workOrders", 0);
        }

        try
        {
            List<WmsStatisticsVO> wmsAlerts = wmsStatisticsService.selectStockAlert();
            todos.put("stockAlerts", wmsAlerts != null ? wmsAlerts.size() : 0);
        }
        catch (Exception e)
        {
            todos.put("stockAlerts", 0);
        }

        try
        {
            List<DmsSparePart> dmsAlerts = sparePartMapper.selectStockAlertList(new DmsSparePart());
            todos.put("partAlerts", dmsAlerts != null ? dmsAlerts.size() : 0);
        }
        catch (Exception e)
        {
            todos.put("partAlerts", 0);
        }

        return todos;
    }

    /**
     * 创建指标Map
     */
    private Map<String, Object> metric(String label, Object value, String unit)
    {
        Map<String, Object> m = new HashMap<>();
        m.put("label", label);
        m.put("value", value);
        m.put("unit", unit);
        return m;
    }

    /**
     * 创建规划中模块
     */
    private Map<String, Object> plannedModule(String code, String name, String icon, String color, String gradient, String desc)
    {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("name", name);
        m.put("icon", icon);
        m.put("color", color);
        m.put("gradient", gradient);
        m.put("status", "planned");
        m.put("path", "");
        m.put("desc", desc);
        m.put("metrics", new ArrayList<>());
        return m;
    }

    /**
     * 获取星期几
     */
    private String getWeekday()
    {
        String[] weekdays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        return weekdays[java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1];
    }
}
