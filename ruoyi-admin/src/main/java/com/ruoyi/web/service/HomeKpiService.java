package com.ruoyi.web.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.web.mapper.HomeKpiMapper;

/**
 * 首页岗位 KPI Service
 *
 * 设计（与"新手上路"保持同一套岗位口径）：
 * 1. 配置在 sys_role_kpi：哪个岗位（role_key）看哪几个指标、什么顺序、点哪个菜单；
 * 2. 取数在 {@link HomeKpiMapper}，一指标一条只读 SQL，口径与各模块列表页一致；
 * 3. 用户有多个岗位时，命中第一个"已配置 KPI"的岗位（与新人清单同规则），避免多岗位拼成 8 张卡；
 * 4. 岗位未命中时回退到通用指标（role_key = {@link #DEFAULT_ROLE_KEY}），当前固定为
 *    物料 / 设备 / 销售订单 / 采购订单 / 工单 五张卡，覆盖"暂时不做岗位差异化"的场景；
 * 5. 连通用配置都没有（未执行 SQL）时返回空列表，前端自动回退到原"按模块"KPI，保证灰度期间界面不变。
 *
 * @author ruoyi
 */
@Service
public class HomeKpiService
{
    /** 通用核心指标配置的虚拟 role_key（不占用真实角色） */
    public static final String DEFAULT_ROLE_KEY = "__default__";

    /** 金额统一折算为万元，避免首页出现 6 位以上数字 */
    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");

    @Autowired
    private HomeKpiMapper kpiMapper;

    /*
     * 注意：顶部 KPI 卡「不跟随模块开关显隐」。
     * 模块关闭后卡片消失会留出空白格，视觉上比"保留卡片"更难看；
     * 因此这里刻意不做模块过滤，模块开关只作用于待办、动态、菜单与常用功能。
     */

    /** 单个指标的展示载体 */
    public static class KpiItem
    {
        public String metricKey;
        public String label;
        public String value = "0";
        public String unit = "";
        public String icon = "chart";
        public String color = "#4f46e5";
        public String sub = "";
        public String subText = "";
        public String targetTitles = "";
        public String targetPath = "";
    }

    /**
     * 构建首页顶部核心指标卡
     *
     * 现状（2026-11-18）：岗位差异化已下线，sys_role_kpi 中只保留 __default__ 五张固定卡，
     * 所有用户统一命中 default；方法保留岗位匹配能力，后续「门户卡片自定义维护」可直接复用。
     *
     * @param alerts 首页预警汇总（复用同一次请求快照，避免重复取数）
     * @param todos  首页待办汇总
     * @return roleName 命中的岗位名称（未命中为空）；roleKey 岗位标识；items 指标卡列表（未配置时为空）
     */
    public Map<String, Object> buildRoleKpis(Map<String, Object> alerts, Map<String, Object> todos)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("roleKey", "");
        result.put("roleName", "");
        result.put("items", new ArrayList<>());

        List<SysRole> roles = currentRoles();
        if (roles == null || roles.isEmpty())
        {
            return buildDefaultKpis(result, alerts, todos);
        }

        for (SysRole role : roles)
        {
            String roleKey = role.getRoleKey();
            if (roleKey == null || roleKey.trim().isEmpty())
            {
                continue;
            }
            List<Map<String, Object>> configs;
            try
            {
                configs = kpiMapper.selectByRoleKey(roleKey);
            }
            catch (Exception e)
            {
                // 表未创建（未执行 sql/home_role_kpi.sql）时不影响首页其它区块
                continue;
            }
            if (configs == null || configs.isEmpty())
            {
                continue;
            }

            List<Map<String, Object>> items = new ArrayList<>();
            for (Map<String, Object> cfg : configs)
            {
                String metricKey = asText(cfg.get("metricKey"));
                KpiItem item = compute(metricKey, alerts, todos);
                if (item == null)
                {
                    continue;
                }
                // 配置可覆盖默认标题/单位/跳转目标
                String title = asText(cfg.get("title"));
                String unit = asText(cfg.get("unit"));
                String titles = asText(cfg.get("targetTitles"));
                String path = asText(cfg.get("targetPath"));
                if (title != null) item.label = title;
                if (unit != null) item.unit = unit;
                if (titles != null) item.targetTitles = titles;
                if (path != null) item.targetPath = path;

                Map<String, Object> map = new HashMap<>();
                map.put("metricKey", item.metricKey);
                map.put("label", item.label);
                map.put("value", item.value);
                map.put("unit", item.unit);
                map.put("icon", item.icon);
                map.put("color", item.color);
                map.put("sub", item.sub);
                map.put("subText", item.subText);
                map.put("targetTitles", item.targetTitles);
                map.put("targetPath", item.targetPath);
                items.add(map);
            }

            if (items.isEmpty())
            {
                continue;
            }
            result.put("roleKey", roleKey);
            result.put("roleName", role.getRoleName() == null ? "" : role.getRoleName());
            result.put("items", items);
            return result;
        }
        // 岗位未命中配置（或岗位化指标暂未启用）→ 回退通用核心指标
        return buildDefaultKpis(result, alerts, todos);
    }

    /**
     * 通用核心指标：所有未命中岗位配置的用户共用一套（role_key = __default__）
     * 目前固定为 物料 / 设备 / 销售订单 / 采购订单 / 工单 五个业务对象，
     * 由 sys_role_kpi 配置驱动，后续要增减或换指标改配置即可，无需改代码。
     */
    private Map<String, Object> buildDefaultKpis(Map<String, Object> result, Map<String, Object> alerts, Map<String, Object> todos)
    {
        try
        {
            List<Map<String, Object>> configs = kpiMapper.selectByRoleKey(DEFAULT_ROLE_KEY);
            if (configs == null || configs.isEmpty())
            {
                return result;
            }
            List<Map<String, Object>> items = new ArrayList<>();
            for (Map<String, Object> cfg : configs)
            {
                KpiItem item = compute(asText(cfg.get("metricKey")), alerts, todos);
                if (item == null)
                {
                    continue;
                }
                String title = asText(cfg.get("title"));
                String unit = asText(cfg.get("unit"));
                String titles = asText(cfg.get("targetTitles"));
                String path = asText(cfg.get("targetPath"));
                if (title != null) item.label = title;
                if (unit != null) item.unit = unit;
                if (titles != null) item.targetTitles = titles;
                if (path != null) item.targetPath = path;

                Map<String, Object> map = new HashMap<>();
                map.put("metricKey", item.metricKey);
                map.put("label", item.label);
                map.put("value", item.value);
                map.put("unit", item.unit);
                map.put("icon", item.icon);
                map.put("color", item.color);
                map.put("sub", item.sub);
                map.put("subText", item.subText);
                map.put("targetTitles", item.targetTitles);
                map.put("targetPath", item.targetPath);
                items.add(map);
            }
            result.put("roleKey", DEFAULT_ROLE_KEY);
            result.put("items", items);
        }
        catch (Exception ignored)
        {
            // 表未创建时保持空列表，首页自动回退到「按模块」KPI
        }
        return result;
    }

    /** 当前登录用户的岗位列表（保持用户分配顺序） */
    private List<SysRole> currentRoles()
    {
        try
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null || loginUser.getUser() == null)
            {
                return null;
            }
            return loginUser.getUser().getRoles();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 指标注册表：编码 → 展示信息 + 取数
     * 新增指标只需在这里加一个 case，并在 sys_role_kpi 里挂到岗位，无需改前端。
     */
    private KpiItem compute(String metricKey, Map<String, Object> alerts, Map<String, Object> todos)
    {
        if (metricKey == null)
        {
            return null;
        }
        KpiItem item = new KpiItem();
        item.metricKey = metricKey;

        try
        {
            switch (metricKey)
            {
                case "FIN_AR_MONTH":
                {
                    Map<String, Object> row = kpiMapper.selectArMonth();
                    item.label = "本月开票收入";
                    item.unit = "万元";
                    item.value = wan(row.get("amount"));
                    item.sub = "本月 " + num(row.get("cnt")) + " 张应收单";
                    item.subText = "含税口径";
                    item.icon = "chart";
                    item.color = "#0ea5e9";
                    break;
                }
                case "FIN_AR_OVERDUE":
                {
                    Map<String, Object> row = kpiMapper.selectArOverdue();
                    item.label = "逾期未收款";
                    item.unit = "万元";
                    item.value = wan(row.get("amount"));
                    item.sub = "逾期 " + num(row.get("cnt")) + " 笔";
                    item.subText = "需重点跟进";
                    item.icon = "money";
                    item.color = "#e9423a";
                    break;
                }
                case "FIN_AP_UNPAID":
                {
                    Map<String, Object> row = kpiMapper.selectApUnpaid();
                    item.label = "应付未付款";
                    item.unit = "万元";
                    item.value = wan(row.get("amount"));
                    item.sub = "未付 " + num(row.get("cnt")) + " 笔";
                    item.subText = "含税口径";
                    item.icon = "money";
                    item.color = "#e8900f";
                    break;
                }
                case "FIN_VOUCHER_DRAFT":
                {
                    Map<String, Object> row = kpiMapper.selectVoucherDraft();
                    item.label = "待审核凭证";
                    item.unit = "张";
                    item.value = num(row.get("cnt"));
                    item.sub = "草稿状态";
                    item.subText = "总账凭证";
                    item.icon = "documentation";
                    item.color = "#2563eb";
                    break;
                }
                case "FIN_PERIOD_OPEN":
                {
                    Map<String, Object> row = kpiMapper.selectPeriodOpen();
                    item.label = "未结账期间";
                    item.unit = "个";
                    item.value = num(row.get("cnt"));
                    item.sub = "状态为 OPEN";
                    item.subText = "及时关账";
                    item.icon = "time";
                    item.color = "#722ed1";
                    break;
                }
                case "ALERT_TOTAL":
                {
                    int total = intOf(alerts.get("total"));
                    item.label = "全平台预警";
                    item.unit = "项";
                    item.value = String.valueOf(total);
                    item.sub = "库存 " + child(alerts, "wms", "total") + " · 设备 " + child(alerts, "dms", "total");
                    item.subText = "质量 " + child(alerts, "qms", "total") + " · 安全 " + child(alerts, "safety", "total");
                    item.icon = "bug";
                    item.color = "#e9423a";
                    break;
                }
                case "PMS_APPROVAL_PENDING":
                {
                    int requests = intOf(todos.get("purchaseRequests"));
                    int orders = intOf(todos.get("purchaseOrders"));
                    item.label = "待审批单据";
                    item.unit = "单";
                    item.value = String.valueOf(requests + orders);
                    item.sub = "申请 " + requests + " · 订单 " + orders;
                    item.subText = "采购流程";
                    item.icon = "clipboard";
                    item.color = "#2563eb";
                    break;
                }
                case "SAFETY_HAZARD_OPEN":
                {
                    Map<String, Object> safety = childMap(alerts, "safety");
                    item.label = "未闭环隐患";
                    item.unit = "项";
                    item.value = String.valueOf(intOf(safety.get("openHazard")));
                    item.sub = "超期 " + intOf(safety.get("overdue")) + " 项";
                    item.subText = "隐患排查";
                    item.icon = "warning";
                    item.color = "#e9423a";
                    break;
                }
                case "QMS_NCR_OPEN":
                {
                    Map<String, Object> qms = childMap(alerts, "qms");
                    item.label = "未闭环NCR";
                    item.unit = "项";
                    item.value = String.valueOf(intOf(qms.get("openNcr")));
                    item.sub = "待验证 " + intOf(qms.get("pendingVerify")) + " 项";
                    item.subText = "质量处置";
                    item.icon = "shield";
                    item.color = "#e8900f";
                    break;
                }
                case "HR_HEADCOUNT":
                {
                    Map<String, Object> row = kpiMapper.selectHrHeadcount();
                    item.label = "在册人数";
                    item.unit = "人";
                    item.value = num(row.get("total"));
                    item.sub = "在职 " + num(row.get("onjob")) + " 人";
                    item.subText = "试用 " + num(row.get("probation")) + " 人";
                    item.icon = "peoples";
                    item.color = "#eb2f96";
                    break;
                }
                case "HR_PROBATION":
                {
                    Map<String, Object> row = kpiMapper.selectHrHeadcount();
                    item.label = "试用期人数";
                    item.unit = "人";
                    item.value = num(row.get("probation"));
                    Map<String, Object> regular = kpiMapper.selectHrRegularPending();
                    item.sub = "待转正 " + num(regular.get("cnt")) + " 人";
                    item.subText = "转正流程";
                    item.icon = "education";
                    item.color = "#722ed1";
                    break;
                }
                case "HR_CONTRACT_EXPIRE":
                {
                    Map<String, Object> row = kpiMapper.selectHrContractExpire();
                    item.label = "合同到期预警";
                    item.unit = "份";
                    item.value = num(row.get("cnt"));
                    item.sub = "30 天内到期";
                    item.subText = "劳动合同";
                    item.icon = "documentation";
                    item.color = "#e8900f";
                    break;
                }
                case "HR_LEAVE_PENDING":
                {
                    Map<String, Object> row = kpiMapper.selectHrLeavePending();
                    item.label = "待审批请假";
                    item.unit = "单";
                    item.value = num(row.get("cnt"));
                    item.sub = "待处理";
                    item.subText = "请假申请";
                    item.icon = "clipboard";
                    item.color = "#0ea5e9";
                    break;
                }
                case "OA_VEHICLE_TOTAL":
                {
                    Map<String, Object> row = kpiMapper.selectOaVehicle();
                    item.label = "公务车辆";
                    item.unit = "辆";
                    item.value = num(row.get("total"));
                    item.sub = "台账在册";
                    item.subText = "车辆管理";
                    item.icon = "guide";
                    item.color = "#0891b2";
                    break;
                }
                case "OA_VEHICLE_DUE":
                {
                    Map<String, Object> row = kpiMapper.selectOaVehicle();
                    item.label = "证件到期预警";
                    item.unit = "辆";
                    item.value = num(row.get("due"));
                    item.sub = "年检/保险 30 天内";
                    item.subText = "需续办";
                    item.icon = "warning";
                    item.color = "#e8900f";
                    break;
                }
                case "OA_SEAL_TOTAL":
                {
                    Map<String, Object> row = kpiMapper.selectOaSeal();
                    item.label = "在册印章";
                    item.unit = "枚";
                    item.value = num(row.get("cnt"));
                    item.sub = "启用中";
                    item.subText = "印章台账";
                    item.icon = "lock";
                    item.color = "#722ed1";
                    break;
                }
                case "MATERIAL_TOTAL":
                {
                    Map<String, Object> row = kpiMapper.selectMaterialTotal();
                    Map<String, Object> warn = kpiMapper.selectStockBelowSafety();
                    item.label = "物料档案";
                    item.unit = "种";
                    item.value = num(row.get("total"));
                    item.sub = "库存预警 " + num(warn.get("cnt")) + " 种";
                    item.subText = "低于安全库存";
                    item.icon = "storage";
                    item.color = "#0891b2";
                    break;
                }
                case "EQUIPMENT_TOTAL":
                {
                    Map<String, Object> row = kpiMapper.selectEquipmentTotal();
                    item.label = "设备台账";
                    item.unit = "台";
                    item.value = num(row.get("total"));
                    item.sub = "在用 " + num(row.get("inuse")) + " · 维修 " + num(row.get("repair"));
                    item.subText = "设备管理";
                    item.icon = "guide";
                    item.color = "#2c8af5";
                    break;
                }
                case "SALES_ORDER_WAIT_SHIP":
                {
                    Map<String, Object> row = kpiMapper.selectSalesOrderWaitShip();
                    item.label = "待发货订单";
                    item.unit = "单";
                    item.value = num(row.get("cnt"));
                    item.sub = "金额 " + wan(row.get("amount")) + " 万元";
                    item.subText = "已审核待发货";
                    item.icon = "money";
                    item.color = "#12a65c";
                    break;
                }
                case "PURCHASE_ORDER_OPEN":
                {
                    Map<String, Object> row = kpiMapper.selectPurchaseOrderOpen();
                    item.label = "在途采购订单";
                    item.unit = "单";
                    item.value = num(row.get("cnt"));
                    item.sub = "待审批 " + num(row.get("pending")) + " 单 · " + wan(row.get("amount")) + " 万";
                    item.subText = "含部分到货";
                    item.icon = "shopping";
                    item.color = "#e8900f";
                    break;
                }
                case "WORK_ORDER_OPEN":
                {
                    Map<String, Object> row = kpiMapper.selectWorkOrderOpen();
                    item.label = "未完工工单";
                    item.unit = "张";
                    item.value = num(row.get("cnt"));
                    item.sub = "执行中 " + num(row.get("doing")) + " · 已下达 " + num(row.get("released"));
                    item.subText = "已下达及执行中";
                    item.icon = "build";
                    item.color = "#722ed1";
                    break;
                }
                default:
                    return null;
            }
        }
        catch (Exception e)
        {
            // 单指标取数失败不拖垮整条指标带
            return null;
        }
        return item;
    }

    /** 金额（元）→ 万元，保留 1 位小数 */
    private String wan(Object yuan)
    {
        BigDecimal amount = BigDecimal.ZERO;
        if (yuan instanceof BigDecimal)
        {
            amount = (BigDecimal) yuan;
        }
        else if (yuan instanceof Number)
        {
            amount = new BigDecimal(String.valueOf(yuan));
        }
        return amount.divide(TEN_THOUSAND, 1, RoundingMode.HALF_UP).toPlainString();
    }

    /** 计数取整字符串（COUNT/SUM 可能返回 Long/Integer/BigDecimal） */
    private String num(Object value)
    {
        return String.valueOf(intOf(value));
    }

    private int intOf(Object value)
    {
        if (value == null)
        {
            return 0;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return new BigDecimal(String.valueOf(value)).intValue();
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> childMap(Map<String, Object> root, String key)
    {
        Object value = root == null ? null : root.get(key);
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    /** 取嵌套 Map 的数值（用于预警分项展示） */
    private int child(Map<String, Object> root, String key, String subKey)
    {
        return intOf(childMap(root, key).get(subKey));
    }

    private String asText(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
