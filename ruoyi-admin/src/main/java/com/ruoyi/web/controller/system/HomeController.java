package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.ruoyi.system.mapper.SysUserHomeLayoutMapper;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import com.ruoyi.wms.service.IWmsStatisticsService;

/**
 * 平台首页聚合数据 Controller
 *
 * 汇总仓库管理(WMS)、设备管理(DMS)、采购管理(PMS)、营销管理(MK)、
 * 生产管控(MMS)、质量管理(QMS)、安全生产管理(Safety)、人力资源(HR)
 * 各业务模块的关键指标，为平台首页提供统一的概览数据；
 * 同时提供用户级门户首页模块显示布局配置。
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
    private SysUserHomeLayoutMapper homeLayoutMapper;

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
     * 获取当前用户门户首页模块显示配置
     */
    @GetMapping("/layout")
    public AjaxResult getLayout()
    {
        Long userId = SecurityUtils.getUserId();
        String codes = null;
        try
        {
            codes = homeLayoutMapper.selectModuleCodesByUserId(userId);
        }
        catch (Exception e)
        {
            codes = null;
        }
        return AjaxResult.success(codes == null ? "" : codes);
    }

    /**
     * 保存当前用户门户首页模块显示配置
     */
    @PutMapping("/layout")
    public AjaxResult saveLayout(@RequestBody Map<String, Object> body)
    {
        Long userId = SecurityUtils.getUserId();
        String codes = body.get("moduleCodes") == null ? "" : String.valueOf(body.get("moduleCodes"));
        if (codes.length() > 500)
        {
            return error("配置内容过长");
        }
        homeLayoutMapper.saveHomeLayout(userId, codes, SecurityUtils.getUsername());
        return success();
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

        // QMS 预警（未闭环NCR）
        Map<String, Object> qmsAlerts = new HashMap<>();
        try
        {
            List<QmsNcr> ncrs = qmsNcrMapper.selectNcrList(new QmsNcr());
            int open = 0, pendingVerify = 0;
            if (ncrs != null)
            {
                for (QmsNcr n : ncrs)
                {
                    String s = n.getNcrStatus();
                    if ("0".equals(s) || "1".equals(s) || "2".equals(s) || "3".equals(s)) open++;
                    if ("3".equals(s)) pendingVerify++;
                }
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

        // Safety 预警（未闭环隐患）
        Map<String, Object> safetyAlerts = new HashMap<>();
        try
        {
            List<SafetyHazard> hazards = safetyHazardMapper.selectSafetyHazardList(new SafetyHazard());
            int open = 0, overdue = 0;
            if (hazards != null)
            {
                for (SafetyHazard h : hazards)
                {
                    String s = h.getHazardStatus();
                    if ("3".equals(s) || "4".equals(s) || "5".equals(s) || "7".equals(s)) open++;
                    if ("7".equals(s)) overdue++;
                }
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
    private List<Map<String, Object>> buildModules()
    {
        List<Map<String, Object>> modules = new ArrayList<>();

        modules.add(buildWmsModule());
        modules.add(buildDmsModule());
        modules.add(buildPmsModule());
        modules.add(buildMkModule());
        modules.add(buildMmsModule());
        modules.add(buildQmsModule());
        modules.add(buildSafetyModule());
        modules.add(buildHrModule());

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
    private Map<String, Object> buildDmsModule()
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
        return dms;
    }

    /**
     * 采购管理 PMS
     * 状态：申请 0草稿/1待审批/2已审批/3已驳回/4已转采购/5已作废
     *       订单 0草稿/1待审批/2已审批/3已下单/4部分到货/5已完成/6已作废
     */
    private Map<String, Object> buildPmsModule()
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
            List<PmsPurchaseRequest> requests = purchaseRequestMapper.selectPurchaseRequestList(new PmsPurchaseRequest());
            if (requests != null)
            {
                for (PmsPurchaseRequest r : requests)
                {
                    if ("1".equals(r.getStatus())) pendingRequests++;
                }
            }
            List<PmsPurchaseOrder> orders = purchaseOrderMapper.selectPurchaseOrderList(new PmsPurchaseOrder());
            if (orders != null)
            {
                for (PmsPurchaseOrder o : orders)
                {
                    String s = o.getStatus();
                    if ("1".equals(s)) pendingOrders++;
                    if ("2".equals(s) || "3".equals(s) || "4".equals(s)) executingOrders++;
                }
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
    private Map<String, Object> buildMkModule()
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
            int customers = 0, activeLeads = 0, convertedLeads = 0;
            List<MkCustomer> customerList = mkCustomerMapper.selectCustomerList(new MkCustomer());
            customers = customerList != null ? customerList.size() : 0;
            List<MkLead> leads = mkLeadMapper.selectLeadList(new MkLead());
            if (leads != null)
            {
                for (MkLead l : leads)
                {
                    String s = l.getLeadStatus();
                    if ("4".equals(s)) convertedLeads++;
                    else if (!"5".equals(s)) activeLeads++;
                }
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
    private Map<String, Object> buildMmsModule()
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
            List<MmsWorkOrder> orders = mmsWorkOrderMapper.selectWorkOrderList(new MmsWorkOrder());
            if (orders != null)
            {
                for (MmsWorkOrder o : orders)
                {
                    String s = o.getStatus();
                    if ("1".equals(s) || "2".equals(s)) inProgress++;
                    if ("3".equals(s)) finished++;
                }
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
    private Map<String, Object> buildQmsModule()
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
            List<QmsNcr> ncrs = qmsNcrMapper.selectNcrList(new QmsNcr());
            if (ncrs != null)
            {
                for (QmsNcr n : ncrs)
                {
                    String s = n.getNcrStatus();
                    if ("0".equals(s) || "1".equals(s) || "2".equals(s) || "3".equals(s)) open++;
                    if ("3".equals(s)) pendingVerify++;
                }
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
    private Map<String, Object> buildSafetyModule()
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
            List<SafetyHazard> hazards = safetyHazardMapper.selectSafetyHazardList(new SafetyHazard());
            if (hazards != null)
            {
                for (SafetyHazard h : hazards)
                {
                    String s = h.getHazardStatus();
                    if ("3".equals(s) || "4".equals(s) || "5".equals(s) || "7".equals(s)) open++;
                    if ("7".equals(s)) overdue++;
                }
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
    private Map<String, Object> buildHrModule()
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
            List<HrEmployee> employees = hrEmployeeMapper.selectHrEmployeeList(new HrEmployee());
            if (employees != null)
            {
                for (HrEmployee e : employees)
                {
                    String s = e.getEmployeeStatus();
                    if ("0".equals(s)) onJob++;
                    else if ("1".equals(s)) probation++;
                }
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
     * 构建待办事项
     */
    private Map<String, Object> buildTodos()
    {
        Map<String, Object> todos = new HashMap<>();

        // 设备待处理工单
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
        try
        {
            List<DmsSparePart> dmsAlerts = sparePartMapper.selectStockAlertList(new DmsSparePart());
            todos.put("partAlerts", dmsAlerts != null ? dmsAlerts.size() : 0);
        }
        catch (Exception e)
        {
            todos.put("partAlerts", 0);
        }

        // DMS 故障设备
        try
        {
            List<DmsEquipment> allEquipment = equipmentMapper.selectEquipmentList(new DmsEquipment());
            int fault = 0;
            for (DmsEquipment eq : allEquipment)
            {
                if ("4".equals(eq.getEquipmentStatus())) fault++;
            }
            todos.put("faultEquipment", fault);
        }
        catch (Exception e)
        {
            todos.put("faultEquipment", 0);
        }

        // 采购申请待审批
        try
        {
            int pending = 0;
            List<PmsPurchaseRequest> requests = purchaseRequestMapper.selectPurchaseRequestList(new PmsPurchaseRequest());
            if (requests != null)
            {
                for (PmsPurchaseRequest r : requests)
                {
                    if ("1".equals(r.getStatus())) pending++;
                }
            }
            todos.put("purchaseRequests", pending);
        }
        catch (Exception e)
        {
            todos.put("purchaseRequests", 0);
        }

        // 采购订单待审批
        try
        {
            int pending = 0;
            List<PmsPurchaseOrder> orders = purchaseOrderMapper.selectPurchaseOrderList(new PmsPurchaseOrder());
            if (orders != null)
            {
                for (PmsPurchaseOrder o : orders)
                {
                    if ("1".equals(o.getStatus())) pending++;
                }
            }
            todos.put("purchaseOrders", pending);
        }
        catch (Exception e)
        {
            todos.put("purchaseOrders", 0);
        }

        // 营销待跟进线索
        try
        {
            int active = 0;
            List<MkLead> leads = mkLeadMapper.selectLeadList(new MkLead());
            if (leads != null)
            {
                for (MkLead l : leads)
                {
                    String s = l.getLeadStatus();
                    if (!"4".equals(s) && !"5".equals(s)) active++;
                }
            }
            todos.put("mkLeads", active);
        }
        catch (Exception e)
        {
            todos.put("mkLeads", 0);
        }

        // 生产执行中工单
        try
        {
            int inProgress = 0;
            List<MmsWorkOrder> orders = mmsWorkOrderMapper.selectWorkOrderList(new MmsWorkOrder());
            if (orders != null)
            {
                for (MmsWorkOrder o : orders)
                {
                    String s = o.getStatus();
                    if ("1".equals(s) || "2".equals(s)) inProgress++;
                }
            }
            todos.put("mmsWorkOrders", inProgress);
        }
        catch (Exception e)
        {
            todos.put("mmsWorkOrders", 0);
        }

        // 质量未闭环NCR
        try
        {
            int open = 0;
            List<QmsNcr> ncrs = qmsNcrMapper.selectNcrList(new QmsNcr());
            if (ncrs != null)
            {
                for (QmsNcr n : ncrs)
                {
                    String s = n.getNcrStatus();
                    if ("0".equals(s) || "1".equals(s) || "2".equals(s) || "3".equals(s)) open++;
                }
            }
            todos.put("qmsNcrs", open);
        }
        catch (Exception e)
        {
            todos.put("qmsNcrs", 0);
        }

        // 安全未闭环隐患
        try
        {
            int open = 0;
            List<SafetyHazard> hazards = safetyHazardMapper.selectSafetyHazardList(new SafetyHazard());
            if (hazards != null)
            {
                for (SafetyHazard h : hazards)
                {
                    String s = h.getHazardStatus();
                    if ("3".equals(s) || "4".equals(s) || "5".equals(s) || "7".equals(s)) open++;
                }
            }
            todos.put("safetyHazards", open);
        }
        catch (Exception e)
        {
            todos.put("safetyHazards", 0);
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
     * 获取星期几
     */
    private String getWeekday()
    {
        String[] weekdays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        return weekdays[java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1];
    }
}
