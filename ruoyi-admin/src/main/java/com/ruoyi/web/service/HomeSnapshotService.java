package com.ruoyi.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.domain.DmsPmPlan;
import com.ruoyi.dms.domain.DmsSparePart;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.mapper.DmsPmPlanMapper;
import com.ruoyi.dms.mapper.DmsSparePartMapper;
import com.ruoyi.dms.mapper.DmsWorkOrderMapper;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkLead;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.mapper.MkLeadMapper;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.pms.domain.PmsPurchaseOrder;
import com.ruoyi.pms.domain.PmsPurchaseRequest;
import com.ruoyi.pms.mapper.PmsPurchaseOrderMapper;
import com.ruoyi.pms.mapper.PmsPurchaseRequestMapper;
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.mapper.QmsNcrMapper;
import com.ruoyi.safety.domain.SafetyHazard;
import com.ruoyi.safety.mapper.SafetyHazardMapper;
import com.ruoyi.system.mapper.SysMenuMapper;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import com.ruoyi.wms.service.IWmsStatisticsService;

/**
 * 平台首页跨模块数据聚合 Service（PRD F1.4 HomeController 重构）
 *
 * 职责：将原 HomeController 直连 12 个业务模块 Mapper 的取数逻辑
 * 原样搬入本 Service（纯搬家、逻辑一行不改），HomeController 仅调用本类，
 * 实现"HomeController 代码中不再 import 任何业务模块的 Mapper"（PRD F1.4 验收）。
 *
 * 取数约定（沿用原实现，PRD F1.4）：
 * 1. 一次 dashboard 请求内，每个跨模块数据源仅全量拉取一次（HomeSnapshot），
 *    由预警汇总/模块概览/待办三处共享，禁止重复拉取；
 * 2. 各指标口径与各模块 StatsProvider 保持一致（决策支持统一指标层），
 *    口径调整时须同步修改对应 Provider；
 * 3. 对外接口与响应结构保持不变，前端零改动。
 *
 * @author ruoyi
 */
@Service
public class HomeSnapshotService
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
    @Autowired
    private PmsPurchaseRequestMapper purchaseRequestMapper;
    @Autowired
    private PmsPurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private MkLeadMapper mkLeadMapper;
    @Autowired
    private MkCustomerMapper mkCustomerMapper;
    @Autowired
    private MmsWorkOrderMapper mmsWorkOrderMapper;
    @Autowired
    private QmsNcrMapper qmsNcrMapper;
    @Autowired
    private SafetyHazardMapper safetyHazardMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;
    @Autowired
    private SysMenuMapper menuMapper;

    /** 业务模块编码 → 顶层菜单ID（用于按菜单停用状态过滤首页模块） */
    private static final Map<String, Long> MODULE_MENU_IDS = new HashMap<>();
    static
    {
        MODULE_MENU_IDS.put("wms", 2000L);
        MODULE_MENU_IDS.put("dms", 3000L);
        MODULE_MENU_IDS.put("mk", 4000L);
        MODULE_MENU_IDS.put("pms", 4197L);
        MODULE_MENU_IDS.put("safety", 4638L);
        MODULE_MENU_IDS.put("qms", 4727L);
        MODULE_MENU_IDS.put("fin", 905938L);
        MODULE_MENU_IDS.put("hr", 905538L);
        MODULE_MENU_IDS.put("mms", 5000L);
    }

    /**
     * 一次请求的跨模块数据快照：各数据源仅拉取一次，
     * 供预警汇总/模块概览/待办三处共享（消除原实现中 2~3 次的重复拉取）
     */
    public static class HomeSnapshot
    {
        List<DmsEquipment> dmsEquipments = new ArrayList<>();
        List<DmsWorkOrder> dmsWorkOrders = new ArrayList<>();
        List<DmsSparePart> dmsSpareAlerts = new ArrayList<>();
        List<QmsNcr> qmsNcrs = new ArrayList<>();
        List<SafetyHazard> safetyHazards = new ArrayList<>();
        List<PmsPurchaseRequest> pmsRequests = new ArrayList<>();
        List<PmsPurchaseOrder> pmsOrders = new ArrayList<>();
        List<MkCustomer> mkCustomers = new ArrayList<>();
        List<MkLead> mkLeads = new ArrayList<>();
        List<MmsWorkOrder> mmsWorkOrders = new ArrayList<>();
        List<HrEmployee> hrEmployees = new ArrayList<>();
    }

    /**
     * 判断模块对应菜单是否启用（status=0 正常；停用或查询异常时返回 false/true 策略：异常时放行）
     */
    private boolean isModuleEnabled(String code)
    {
        Long menuId = MODULE_MENU_IDS.get(code);
        if (menuId == null)
        {
            return true;
        }
        try
        {
            SysMenu menu = menuMapper.selectMenuById(menuId);
            return menu != null && !"1".equals(menu.getStatus());
        }
        catch (Exception e)
        {
            return true;
        }
    }

    /**
     * 首页聚合数据（原 HomeController./dashboard 方法体原样搬入，
     * 接口 URL /home/dashboard 与响应结构不变，前端零改动）
     *
     * @param userId   当前登录用户ID（welcome 信息）
     * @param username 当前登录用户账号（welcome 信息）
     */
    public Map<String, Object> buildDashboardData(Long userId, String username)
    {
        Map<String, Object> result = new HashMap<>();

        // 1. 用户欢迎信息
        Map<String, Object> welcome = new HashMap<>();
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

        // 2. 跨模块数据单次拉取（预警汇总/模块概览/待办三处共享）
        HomeSnapshot snap = loadSnapshot();

        // 3. 预警汇总
        Map<String, Object> alerts = buildAlertSummary(snap);
        result.put("alerts", alerts);

        // 4. 业务模块概览
        List<Map<String, Object>> modules = buildModules(snap);
        result.put("modules", modules);

        // 5. 待办事项
        Map<String, Object> todos = buildTodos(snap);
        result.put("todos", todos);

        // 6. WMS出入库趋势（近7天）
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

        return result;
    }

    /**
     * 单次拉取全部跨模块数据（逐源容错：单模块异常不影响首页整体可用性）
     */
    public HomeSnapshot loadSnapshot()
    {
        HomeSnapshot snap = new HomeSnapshot();
        try
        {
            snap.dmsEquipments = equipmentMapper.selectEquipmentList(new DmsEquipment());
        }
        catch (Exception e) { }
        try
        {
            snap.dmsWorkOrders = workOrderMapper.selectWorkOrderList(new DmsWorkOrder());
        }
        catch (Exception e) { }
        try
        {
            snap.dmsSpareAlerts = sparePartMapper.selectStockAlertList(new DmsSparePart());
        }
        catch (Exception e) { }
        try
        {
            snap.qmsNcrs = qmsNcrMapper.selectNcrList(new QmsNcr());
        }
        catch (Exception e) { }
        try
        {
            snap.safetyHazards = safetyHazardMapper.selectSafetyHazardList(new SafetyHazard());
        }
        catch (Exception e) { }
        try
        {
            snap.pmsRequests = purchaseRequestMapper.selectPurchaseRequestList(new PmsPurchaseRequest());
        }
        catch (Exception e) { }
        try
        {
            snap.pmsOrders = purchaseOrderMapper.selectPurchaseOrderList(new PmsPurchaseOrder());
        }
        catch (Exception e) { }
        try
        {
            snap.mkCustomers = mkCustomerMapper.selectCustomerList(new MkCustomer());
        }
        catch (Exception e) { }
        try
        {
            snap.mkLeads = mkLeadMapper.selectLeadList(new MkLead());
        }
        catch (Exception e) { }
        try
        {
            snap.mmsWorkOrders = mmsWorkOrderMapper.selectWorkOrderList(new MmsWorkOrder());
        }
        catch (Exception e) { }
        try
        {
            snap.hrEmployees = hrEmployeeMapper.selectHrEmployeeList(new HrEmployee());
        }
        catch (Exception e) { }
        return snap;
    }

    /**
     * 构建预警汇总
     */
    public Map<String, Object> buildAlertSummary(HomeSnapshot snap)
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

        // DMS 预警（复用快照，口径：备件预警 + 故障设备(状态4) + 待处理工单(状态0/1)）
        Map<String, Object> dmsAlerts = new HashMap<>();
        try
        {
            int partAlertCount = snap.dmsSpareAlerts.size();

            int faultEquipment = 0;
            for (DmsEquipment eq : snap.dmsEquipments)
            {
                if ("4".equals(eq.getEquipmentStatus())) faultEquipment++;
            }

            int pendingWorkOrders = countPendingDmsOrders(snap);

            dmsAlerts.put("partAlert", partAlertCount);
            dmsAlerts.put("faultEquipment", faultEquipment);
            dmsAlerts.put("pendingWorkOrder", pendingWorkOrders);
            dmsAlerts.put("total", partAlertCount + faultEquipment + pendingWorkOrders);
            total += partAlertCount + faultEquipment + pendingWorkOrders;
        }
        catch (Exception e)
        {
            dmsAlerts.put("total", 0);
        }
        alerts.put("dms", dmsAlerts);

        // QMS 预警（未闭环NCR，口径与 QmsStatsProvider.QMS_NCR_OPEN 一致：状态≠4 关闭）
        Map<String, Object> qmsAlerts = new HashMap<>();
        try
        {
            int open = 0, pendingVerify = 0;
            for (QmsNcr n : snap.qmsNcrs)
            {
                String s = n.getNcrStatus();
                if ("0".equals(s) || "1".equals(s) || "2".equals(s) || "3".equals(s)) open++;
                if ("3".equals(s)) pendingVerify++;
            }
            qmsAlerts.put("openNcr", open);
            qmsAlerts.put("pendingVerify", pendingVerify);
            qmsAlerts.put("total", open);
            total += open;
        }
        catch (Exception e)
        {
            qmsAlerts.put("total", 0);
        }
        alerts.put("qms", qmsAlerts);

        // Safety 预警（未闭环隐患，口径与 SafetyStatsProvider 一致：状态 3/4/5/7）
        Map<String, Object> safetyAlerts = new HashMap<>();
        try
        {
            int open = 0, overdue = 0;
            for (SafetyHazard h : snap.safetyHazards)
            {
                String s = h.getHazardStatus();
                if ("3".equals(s) || "4".equals(s) || "5".equals(s) || "7".equals(s)) open++;
                if ("7".equals(s)) overdue++;
            }
            safetyAlerts.put("openHazard", open);
            safetyAlerts.put("overdue", overdue);
            safetyAlerts.put("total", open);
            total += open;
        }
        catch (Exception e)
        {
            safetyAlerts.put("total", 0);
        }
        alerts.put("safety", safetyAlerts);

        alerts.put("total", total);
        return alerts;
    }

    /**
     * 构建业务模块概览
     */
    public List<Map<String, Object>> buildModules(HomeSnapshot snap)
    {
        List<Map<String, Object>> modules = new ArrayList<>();

        // 仅返回菜单处于启用状态的模块（临时关闭的模块不再出现在首页）
        if (isModuleEnabled("wms")) modules.add(buildWmsModule());
        if (isModuleEnabled("dms")) modules.add(buildDmsModule(snap));
        if (isModuleEnabled("pms")) modules.add(buildPmsModule(snap));
        if (isModuleEnabled("mk")) modules.add(buildMkModule(snap));
        if (isModuleEnabled("mms")) modules.add(buildMmsModule(snap));
        if (isModuleEnabled("qms")) modules.add(buildQmsModule(snap));
        if (isModuleEnabled("safety")) modules.add(buildSafetyModule(snap));
        if (isModuleEnabled("hr")) modules.add(buildHrModule(snap));

        return modules;
    }

    /**
     * 仓库管理 WMS
     */
    private Map<String, Object> buildWmsModule()
    {
        Map<String, Object> wms = new HashMap<>();
        wms.put("code", "wms");
        wms.put("name", "仓库管理");
        wms.put("icon", "shopping");
        wms.put("color", "#4f46e5");
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
        return wms;
    }

    /**
     * 设备管理 DMS
     */
    private Map<String, Object> buildDmsModule(HomeSnapshot snap)
    {
        Map<String, Object> dms = new HashMap<>();
        dms.put("code", "dms");
        dms.put("name", "设备管理");
        dms.put("icon", "server");
        dms.put("color", "#0891b2");
        dms.put("status", "active");
        dms.put("path", "/dms/dashboard/overview");
        dms.put("desc", "设备台账、工单、备件管理");
        List<Map<String, Object>> dmsMetrics = new ArrayList<>();
        try
        {
            int eqTotal = snap.dmsEquipments.size();
            int eqInUse = 0;
            for (DmsEquipment eq : snap.dmsEquipments)
            {
                if ("0".equals(eq.getEquipmentStatus())) eqInUse++;
            }
            double usageRate = eqTotal > 0 ? (eqInUse * 100.0 / eqTotal) : 0;

            dmsMetrics.add(metric("设备总数", eqTotal, "台"));
            dmsMetrics.add(metric("在用率", String.format("%.1f", usageRate), "%"));
            dmsMetrics.add(metric("待处理工单", countPendingDmsOrders(snap), "张"));
            dmsMetrics.add(metric("备件预警", snap.dmsSpareAlerts.size(), "项"));
        }
        catch (Exception e)
        {
            dmsMetrics.add(metric("设备总数", 0, "台"));
            dmsMetrics.add(metric("在用率", "0.0", "%"));
            dmsMetrics.add(metric("待处理工单", 0, "张"));
            dmsMetrics.add(metric("备件预警", 0, "项"));
        }
        dms.put("metrics", dmsMetrics);
        return dms;
    }

    /**
     * 采购管理 PMS
     * 状态：申请 0草稿/1待审批/2已审批/3已驳回/4已转采购/5已作废
     *       订单 0草稿/1待审批/2已审批/3已下单/4部分到货/5已完成/6已作废
     */
    private Map<String, Object> buildPmsModule(HomeSnapshot snap)
    {
        Map<String, Object> pms = new HashMap<>();
        pms.put("code", "pms");
        pms.put("name", "采购管理");
        pms.put("icon", "money");
        pms.put("color", "#2563eb");
        pms.put("status", "active");
        pms.put("path", "/pms/request");
        pms.put("desc", "采购申请、订单、供应商管理");
        List<Map<String, Object>> metrics = new ArrayList<>();
        try
        {
            int pendingRequests = 0, pendingOrders = 0, executingOrders = 0;
            for (PmsPurchaseRequest r : snap.pmsRequests)
            {
                if ("1".equals(r.getStatus())) pendingRequests++;
            }
            for (PmsPurchaseOrder o : snap.pmsOrders)
            {
                String s = o.getStatus();
                if ("1".equals(s)) pendingOrders++;
                if ("2".equals(s) || "3".equals(s) || "4".equals(s)) executingOrders++;
            }
            metrics.add(metric("待审批申请", pendingRequests, "单"));
            metrics.add(metric("待审批订单", pendingOrders, "单"));
            metrics.add(metric("执行中订单", executingOrders, "单"));
        }
        catch (Exception e)
        {
            metrics.add(metric("待审批申请", 0, "单"));
            metrics.add(metric("待审批订单", 0, "单"));
            metrics.add(metric("执行中订单", 0, "单"));
        }
        pms.put("metrics", metrics);
        return pms;
    }

    /**
     * 营销管理 MK
     * 线索状态：0新线索/1培育中/2MQL/3SQL/4已转化/5已无效
     */
    private Map<String, Object> buildMkModule(HomeSnapshot snap)
    {
        Map<String, Object> mk = new HashMap<>();
        mk.put("code", "mk");
        mk.put("name", "营销管理");
        mk.put("icon", "peoples");
        mk.put("color", "#0ea5e9");
        mk.put("status", "active");
        mk.put("path", "/mk/customer");
        mk.put("desc", "线索、客户、商机、销售订单管理");
        List<Map<String, Object>> metrics = new ArrayList<>();
        try
        {
            int customers = snap.mkCustomers.size();
            int activeLeads = 0, convertedLeads = 0;
            for (MkLead l : snap.mkLeads)
            {
                String s = l.getLeadStatus();
                if ("4".equals(s)) convertedLeads++;
                else if (!"5".equals(s)) activeLeads++;
            }
            metrics.add(metric("客户总数", customers, "家"));
            metrics.add(metric("跟进中线索", activeLeads, "条"));
            metrics.add(metric("已转化线索", convertedLeads, "条"));
        }
        catch (Exception e)
        {
            metrics.add(metric("客户总数", 0, "家"));
            metrics.add(metric("跟进中线索", 0, "条"));
            metrics.add(metric("已转化线索", 0, "条"));
        }
        mk.put("metrics", metrics);
        return mk;
    }

    /**
     * 生产管控 MMS
     * 工单状态：0草稿/1已下达/2执行中/3已完工/4已关闭/5已暂停/6已作废
     */
    private Map<String, Object> buildMmsModule(HomeSnapshot snap)
    {
        Map<String, Object> mms = new HashMap<>();
        mms.put("code", "mms");
        mms.put("name", "生产管控");
        mms.put("icon", "build");
        mms.put("color", "#722ed1");
        mms.put("status", "active");
        mms.put("path", "/mms/workorder");
        mms.put("desc", "生产计划、工单、工序管理");
        List<Map<String, Object>> metrics = new ArrayList<>();
        try
        {
            int inProgress = 0, finished = 0;
            for (MmsWorkOrder o : snap.mmsWorkOrders)
            {
                String s = o.getStatus();
                if ("1".equals(s) || "2".equals(s)) inProgress++;
                if ("3".equals(s)) finished++;
            }
            metrics.add(metric("执行中工单", inProgress, "张"));
            metrics.add(metric("已完工工单", finished, "张"));
        }
        catch (Exception e)
        {
            metrics.add(metric("执行中工单", 0, "张"));
            metrics.add(metric("已完工工单", 0, "张"));
        }
        mms.put("metrics", metrics);
        return mms;
    }

    /**
     * 质量管理 QMS
     * NCR状态：0已登记/1评审中/2处置中/3待验证/4已关闭/5已作废
     */
    private Map<String, Object> buildQmsModule(HomeSnapshot snap)
    {
        Map<String, Object> qms = new HashMap<>();
        qms.put("code", "qms");
        qms.put("name", "质量管理");
        qms.put("icon", "shield");
        qms.put("color", "#e8900f");
        qms.put("status", "active");
        qms.put("path", "/qms/ncr");
        qms.put("desc", "质检任务、NCR、CAPA管理");
        List<Map<String, Object>> metrics = new ArrayList<>();
        try
        {
            int open = 0, pendingVerify = 0;
            for (QmsNcr n : snap.qmsNcrs)
            {
                String s = n.getNcrStatus();
                if ("0".equals(s) || "1".equals(s) || "2".equals(s) || "3".equals(s)) open++;
                if ("3".equals(s)) pendingVerify++;
            }
            metrics.add(metric("未闭环NCR", open, "项"));
            metrics.add(metric("待验证NCR", pendingVerify, "项"));
        }
        catch (Exception e)
        {
            metrics.add(metric("未闭环NCR", 0, "项"));
            metrics.add(metric("待验证NCR", 0, "项"));
        }
        qms.put("metrics", metrics);
        return qms;
    }

    /**
     * 安全生产管理 Safety
     * 隐患状态：0草稿/1待审批/2驳回/3待整改/4整改中/5待验收/6已闭环/7超期未整改
     */
    private Map<String, Object> buildSafetyModule(HomeSnapshot snap)
    {
        Map<String, Object> safety = new HashMap<>();
        safety.put("code", "safety");
        safety.put("name", "安全生产管理");
        safety.put("icon", "warning");
        safety.put("color", "#e9423a");
        safety.put("status", "active");
        safety.put("path", "/safety/dualcontrol/hazard");
        safety.put("desc", "风险分级管控、隐患排查治理");
        List<Map<String, Object>> metrics = new ArrayList<>();
        try
        {
            int open = 0, overdue = 0;
            for (SafetyHazard h : snap.safetyHazards)
            {
                String s = h.getHazardStatus();
                if ("3".equals(s) || "4".equals(s) || "5".equals(s) || "7".equals(s)) open++;
                if ("7".equals(s)) overdue++;
            }
            metrics.add(metric("未闭环隐患", open, "项"));
            metrics.add(metric("超期未整改", overdue, "项"));
        }
        catch (Exception e)
        {
            metrics.add(metric("未闭环隐患", 0, "项"));
            metrics.add(metric("超期未整改", 0, "项"));
        }
        safety.put("metrics", metrics);
        return safety;
    }

    /**
     * 人力资源 HR
     * 员工状态：0在职/1试用期/2离职/3黑名单
     */
    private Map<String, Object> buildHrModule(HomeSnapshot snap)
    {
        Map<String, Object> hr = new HashMap<>();
        hr.put("code", "hr");
        hr.put("name", "人力资源");
        hr.put("icon", "education");
        hr.put("color", "#eb2f96");
        hr.put("status", "active");
        hr.put("path", "/hr/employee");
        hr.put("desc", "员工、考勤、薪酬管理");
        List<Map<String, Object>> metrics = new ArrayList<>();
        try
        {
            int onJob = 0, probation = 0;
            for (HrEmployee e : snap.hrEmployees)
            {
                String s = e.getEmployeeStatus();
                if ("0".equals(s)) onJob++;
                else if ("1".equals(s)) probation++;
            }
            metrics.add(metric("在职员工", onJob, "人"));
            metrics.add(metric("试用期员工", probation, "人"));
        }
        catch (Exception e)
        {
            metrics.add(metric("在职员工", 0, "人"));
            metrics.add(metric("试用期员工", 0, "人"));
        }
        hr.put("metrics", metrics);
        return hr;
    }

    /**
     * 构建待办事项（全部复用快照，不再重复拉取）
     */
    public Map<String, Object> buildTodos(HomeSnapshot snap)
    {
        Map<String, Object> todos = new HashMap<>();

        // 设备待处理工单
        todos.put("workOrders", countPendingDmsOrders(snap));

        // WMS 库存预警
        try
        {
            List<WmsStatisticsVO> wmsAlerts = wmsStatisticsService.selectStockAlert();
            todos.put("stockAlerts", wmsAlerts != null ? wmsAlerts.size() : 0);
        }
        catch (Exception e)
        {
            todos.put("stockAlerts", 0);
        }

        // DMS 备件预警
        todos.put("partAlerts", snap.dmsSpareAlerts.size());

        // DMS 故障设备
        int fault = 0;
        for (DmsEquipment eq : snap.dmsEquipments)
        {
            if ("4".equals(eq.getEquipmentStatus())) fault++;
        }
        todos.put("faultEquipment", fault);

        // 采购申请待审批
        int pendingRequests = 0;
        for (PmsPurchaseRequest r : snap.pmsRequests)
        {
            if ("1".equals(r.getStatus())) pendingRequests++;
        }
        todos.put("purchaseRequests", pendingRequests);

        // 采购订单待审批
        int pendingOrders = 0;
        for (PmsPurchaseOrder o : snap.pmsOrders)
        {
            if ("1".equals(o.getStatus())) pendingOrders++;
        }
        todos.put("purchaseOrders", pendingOrders);

        // 营销待跟进线索
        int activeLeads = 0;
        for (MkLead l : snap.mkLeads)
        {
            String s = l.getLeadStatus();
            if (!"4".equals(s) && !"5".equals(s)) activeLeads++;
        }
        todos.put("mkLeads", activeLeads);

        // 生产执行中工单
        int inProgress = 0;
        for (MmsWorkOrder o : snap.mmsWorkOrders)
        {
            String s = o.getStatus();
            if ("1".equals(s) || "2".equals(s)) inProgress++;
        }
        todos.put("mmsWorkOrders", inProgress);

        // 质量未闭环NCR（口径与 QmsStatsProvider.QMS_NCR_OPEN 一致）
        int openNcrs = 0;
        for (QmsNcr n : snap.qmsNcrs)
        {
            String s = n.getNcrStatus();
            if ("0".equals(s) || "1".equals(s) || "2".equals(s) || "3".equals(s)) openNcrs++;
        }
        todos.put("qmsNcrs", openNcrs);

        // 安全未闭环隐患（口径与 SafetyStatsProvider 一致）
        int openHazards = 0;
        for (SafetyHazard h : snap.safetyHazards)
        {
            String s = h.getHazardStatus();
            if ("3".equals(s) || "4".equals(s) || "5".equals(s) || "7".equals(s)) openHazards++;
        }
        todos.put("safetyHazards", openHazards);

        return todos;
    }

    /**
     * DMS 待处理工单数（口径：状态 0=待派工/1=处理中；预警汇总/模块概览/待办三处共用）
     */
    private int countPendingDmsOrders(HomeSnapshot snap)
    {
        int pending = 0;
        for (DmsWorkOrder o : snap.dmsWorkOrders)
        {
            String s = o.getOrderStatus();
            if ("0".equals(s) || "1".equals(s)) pending++;
        }
        return pending;
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
     * 获取星期几
     */
    private String getWeekday()
    {
        String[] weekdays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        return weekdays[java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1];
    }
}
