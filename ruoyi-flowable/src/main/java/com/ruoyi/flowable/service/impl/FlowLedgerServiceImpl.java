package com.ruoyi.flowable.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.flowable.domain.FlowForm;
import com.ruoyi.flowable.domain.FlowLedger;
import com.ruoyi.flowable.domain.FlowLedgerGrant;
import com.ruoyi.flowable.mapper.FlowFormMapper;
import com.ruoyi.flowable.mapper.FlowLedgerMapper;
import com.ruoyi.flowable.service.IFlowLedgerService;
import com.ruoyi.system.mapper.SysMenuMapper;

/**
 * 流程台账 Service 实现（虚拟台账：配置化列 + 授权范围实时查询流程数据）
 *
 * 数据来源：flow_form 生成的主表 flow_data_xxx（业务字段）
 *          + ACT_HI_PROCINST（流程标题/发起人/发起时间/结束时间/状态，实时取，避免状态漂移）
 *          + flow_instance_ext（发起时冻结的发起部门）
 *
 * @author ruoyi
 */
@Service
public class FlowLedgerServiceImpl implements IFlowLedgerService
{
    private static final Logger log = LoggerFactory.getLogger(FlowLedgerServiceImpl.class);

    /** 主表名白名单（防注入） */
    private static final Pattern TABLE_PATTERN = Pattern.compile("^flow_data_[a-z0-9_]{1,50}$");

    /** 字段名白名单（防注入） */
    private static final Pattern FIELD_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,63}$");

    /** 流程状态表达式（与 FlowProcessServiceImpl 口径一致：结束且有删除原因=已撤销） */
    private static final String STATUS_SQL = "CASE WHEN p.ID_ IS NULL THEN '' "
            + "WHEN p.END_TIME_ IS NULL THEN 'running' "
            + "WHEN p.DELETE_REASON_ IS NOT NULL AND p.DELETE_REASON_ <> '' THEN 'canceled' "
            + "ELSE 'completed' END";

    /** 发起部门表达式（优先取发起时冻结部门，历史数据回退当前用户部门） */
    private static final String DEPT_SQL = "COALESCE(e.start_dept_id, u.dept_id)";

    /** 系统字段定义（字段 → 查询表达式），顺序即候选顺序 */
    private static final Map<String, String> SYS_FIELDS = new LinkedHashMap<>();
    private static final Map<String, String> SYS_LABELS = new LinkedHashMap<>();
    static
    {
        SYS_FIELDS.put("processNo", "d.`processNo`");
        SYS_FIELDS.put("title", "p.NAME_");
        SYS_FIELDS.put("status", STATUS_SQL);
        SYS_FIELDS.put("startUserName", "u.nick_name");
        SYS_FIELDS.put("startDeptName", "dept.dept_name");
        SYS_FIELDS.put("startTime", "p.START_TIME_");
        SYS_FIELDS.put("endTime", "p.END_TIME_");
        SYS_FIELDS.put("requestId", "r.request_id");
        SYS_LABELS.put("processNo", "流程编号");
        SYS_LABELS.put("title", "流程标题");
        SYS_LABELS.put("status", "流程状态");
        SYS_LABELS.put("startUserName", "发起人");
        SYS_LABELS.put("startDeptName", "发起部门");
        SYS_LABELS.put("startTime", "发起时间");
        SYS_LABELS.put("endTime", "结束时间");
        SYS_LABELS.put("requestId", "流程请求ID");
    }

    /** 表单数据表内部列：不作为台账业务字段候选 */
    private static final Set<String> INTERNAL_COLUMNS = new HashSet<>(Arrays.asList(
            "id", "process_instance_id", "create_by", "create_time",
            "title", "processNo", "urgency", "signOpinion", "printCount", "procStatus"));

    private static final Set<String> GRANT_TYPES = new HashSet<>(Arrays.asList("all", "company", "dept", "role", "user"));

    private static final Set<String> SCOPE_TYPES = new HashSet<>(Arrays.asList(
            "all", "self", "dept", "deptAndChild", "company", "companyAndChild", "customDept", "customUser"));

    /** 前台「流程台账」目录菜单ID：每个台账在其下生成一个二级菜单 */
    private static final long LEDGER_MENU_PARENT_ID = 9064600L;
    private static final String LEDGER_MENU_COMPONENT = "flowable/ledger/portal";
    private static final String LEDGER_MENU_PERMS = "flowable:ledger:view";
    private static final String LEDGER_MENU_ICON = "table";

    /** 表结构缓存（表名 → 列名集合），设置 TTL 避免表单新增字段后缓存长期不刷新 */
    private static final long TABLE_COLUMN_CACHE_TTL_MS = 60_000L;
    private static final Map<String, TableColumnCache> TABLE_COLUMN_CACHE = new ConcurrentHashMap<>();

    /**
     * 引擎表流程实例ID列的字符集白名单。排序规则取探测到的实际值（utf8mb3_bin /
     * utf8mb4_general_ci 等），只做格式校验，不写死具体排序规则。
     */
    private static final Set<String> INSTANCE_CHARSETS = new HashSet<>(Arrays.asList("utf8mb3", "utf8mb4", "utf8"));

    /** 字符集/排序规则名称格式白名单（防注入） */
    private static final Pattern COLLATION_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,50}$");

    private static final long INSTANCE_COLLATION_CACHE_TTL_MS = 5 * 60_000L;
    private static volatile String[] INSTANCE_COLLATION_CACHE;
    private static volatile long INSTANCE_COLLATION_EXPIRE_AT;

    private static class TableColumnCache
    {
        private final long expireAt;
        private final Set<String> columns;

        private TableColumnCache(long expireAt, Set<String> columns)
        {
            this.expireAt = expireAt;
            this.columns = columns;
        }
    }

    @Autowired
    private FlowLedgerMapper ledgerMapper;

    @Autowired
    private FlowFormMapper formMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SysMenuMapper menuMapper;

    // ============================================================
    // 后台：台账定义 CRUD
    // ============================================================

    @Override
    public List<FlowLedger> selectFlowLedgerList(FlowLedger query)
    {
        return ledgerMapper.selectFlowLedgerList(query);
    }

    @Override
    public FlowLedger selectFlowLedgerById(Long ledgerId)
    {
        FlowLedger ledger = ledgerMapper.selectFlowLedgerById(ledgerId);
        if (ledger == null)
        {
            throw new ServiceException("台账不存在或已删除");
        }
        return ledger;
    }

    @Override
    @Transactional
    public int insertFlowLedger(FlowLedger ledger)
    {
        applyForm(ledger);
        if (StringUtils.isEmpty(ledger.getLedgerKey()))
        {
            ledger.setLedgerKey(nextLedgerKey(ledger.getFormKey()));
        }
        else if (ledgerMapper.countByLedgerKey(ledger.getLedgerKey()) > 0)
        {
            throw new ServiceException("台账标识已存在：" + ledger.getLedgerKey());
        }
        validateColumns(ledger);
        if (ledger.getExportFlag() == null)
        {
            ledger.setExportFlag("1");
        }
        if (ledger.getStatus() == null)
        {
            ledger.setStatus("0");
        }
        if (ledger.getOrderNum() == null)
        {
            ledger.setOrderNum(0);
        }
        ledger.setDelFlag("0");
        ledger.setCreateBy(SecurityUtils.getUsername());
        ledger.setCreateTime(DateUtils.getNowDate());
        int rows = ledgerMapper.insertFlowLedger(ledger);
        createLedgerMenu(ledger);
        return rows;
    }

    @Override
    @Transactional
    public int updateFlowLedger(FlowLedger ledger)
    {
        FlowLedger existing = selectFlowLedgerById(ledger.getLedgerId());
        // 表名/流程Key始终以绑定表单为准，避免前端未回传导致列校验失败
        applyForm(ledger);
        if (StringUtils.isNotEmpty(ledger.getColumnsJson()))
        {
            validateColumns(ledger);
        }
        ledger.setUpdateBy(SecurityUtils.getUsername());
        ledger.setUpdateTime(DateUtils.getNowDate());
        int rows = ledgerMapper.updateFlowLedger(ledger);
        syncLedgerMenu(ledger, existing.getMenuId());
        return rows;
    }

    @Override
    @Transactional
    public int deleteFlowLedgerByIds(Long[] ledgerIds)
    {
        for (Long ledgerId : ledgerIds)
        {
            FlowLedger ledger = ledgerMapper.selectFlowLedgerById(ledgerId);
            ledgerMapper.deleteGrantByLedgerId(ledgerId);
            if (ledger != null)
            {
                removeLedgerMenu(ledger.getMenuId());
            }
        }
        return ledgerMapper.deleteFlowLedgerByIds(ledgerIds);
    }

    // ============================================================
    // 前台菜单：一个台账对应「流程台账」目录下的一个二级菜单
    // ============================================================

    /** 新增台账后创建前台二级菜单，并沿用「流程台账」目录的角色授权 */
    private void createLedgerMenu(FlowLedger ledger)
    {
        if (ledger.getLedgerId() == null)
        {
            return;
        }
        if (menuMapper.selectMenuById(LEDGER_MENU_PARENT_ID) == null)
        {
            log.warn("流程台账目录菜单不存在（menu_id={}），跳过台账菜单创建", LEDGER_MENU_PARENT_ID);
            return;
        }
        SysMenu menu = new SysMenu();
        menu.setParentId(LEDGER_MENU_PARENT_ID);
        menu.setMenuName(ledger.getLedgerName());
        menu.setOrderNum(menuOrderNum(ledger));
        menu.setPath("l" + ledger.getLedgerId());
        menu.setComponent(LEDGER_MENU_COMPONENT);
        menu.setQuery(menuQuery(ledger.getLedgerId()));
        menu.setIsFrame("1");
        menu.setIsCache("0");
        menu.setMenuType("C");
        menu.setVisible("0");
        menu.setStatus(menuStatus(ledger.getStatus()));
        menu.setPerms(LEDGER_MENU_PERMS);
        menu.setIcon(LEDGER_MENU_ICON);
        menu.setCreateBy(SecurityUtils.getUsername());
        menuMapper.insertMenu(menu);
        if (menu.getMenuId() == null)
        {
            log.warn("台账[{}]前台菜单创建失败：未获取到菜单ID", ledger.getLedgerKey());
            return;
        }
        ledgerMapper.updateLedgerMenuId(ledger.getLedgerId(), menu.getMenuId());
        ledger.setMenuId(menu.getMenuId());
        jdbcTemplate.update("insert ignore into sys_role_menu(role_id, menu_id) "
                + "select distinct role_id, ? from sys_role_menu where menu_id = ?",
                menu.getMenuId(), LEDGER_MENU_PARENT_ID);
    }

    /** 编辑台账后同步菜单名称、顺序、状态与路由参数；菜单缺失时自动补建 */
    private void syncLedgerMenu(FlowLedger ledger, Long menuId)
    {
        if (menuId == null)
        {
            createLedgerMenu(ledger);
            return;
        }
        SysMenu menu = menuMapper.selectMenuById(menuId);
        if (menu == null)
        {
            createLedgerMenu(ledger);
            return;
        }
        menu.setMenuName(ledger.getLedgerName());
        menu.setOrderNum(menuOrderNum(ledger));
        menu.setStatus(menuStatus(ledger.getStatus()));
        menu.setQuery(menuQuery(ledger.getLedgerId()));
        menu.setComponent(LEDGER_MENU_COMPONENT);
        menu.setUpdateBy(SecurityUtils.getUsername());
        menuMapper.updateMenu(menu);
    }

    /** 删除台账时一并清理前台菜单与角色授权 */
    private void removeLedgerMenu(Long menuId)
    {
        if (menuId == null)
        {
            return;
        }
        jdbcTemplate.update("delete from sys_role_menu where menu_id = ?", menuId);
        menuMapper.deleteMenuById(menuId);
    }

    private Integer menuOrderNum(FlowLedger ledger)
    {
        if (ledger.getOrderNum() != null)
        {
            return ledger.getOrderNum();
        }
        return ledger.getLedgerId() == null ? 0 : ledger.getLedgerId().intValue();
    }

    private String menuStatus(String status)
    {
        return "1".equals(status) ? "1" : "0";
    }

    private String menuQuery(Long ledgerId)
    {
        return "{\"ledgerId\":" + ledgerId + "}";
    }

    /** 台账绑定表单：表名/流程Key一律从表单定义取，避免前端传错 */
    private void applyForm(FlowLedger ledger)
    {
        if (ledger.getFormId() == null)
        {
            throw new ServiceException("请选择绑定的流程表单");
        }
        FlowForm form = formMapper.selectFlowFormByFormId(ledger.getFormId());
        if (form == null)
        {
            throw new ServiceException("流程表单不存在");
        }
        if (StringUtils.isEmpty(form.getTableName()))
        {
            throw new ServiceException("该表单尚未生成数据表，请先在表单管理中生成数据表");
        }
        ledger.setFormKey(form.getFormKey());
        ledger.setProcessKey(form.getProcessKey());
        ledger.setTableName(form.getTableName());
        if (StringUtils.isEmpty(ledger.getLedgerName()))
        {
            ledger.setLedgerName(form.getFormName() + "台账");
        }
        if (StringUtils.isEmpty(ledger.getCategory()))
        {
            ledger.setCategory(form.getCategory());
        }
    }

    private String nextLedgerKey(String formKey)
    {
        String base = StringUtils.isEmpty(formKey) ? "ledger" : formKey;
        String key = base;
        int i = 1;
        while (ledgerMapper.countByLedgerKey(key) > 0)
        {
            key = base + "_" + (++i);
        }
        return key;
    }

    /** 列配置校验：来源/字段名白名单，防止 SQL 注入 */
    private void validateColumns(FlowLedger ledger)
    {
        if (StringUtils.isEmpty(ledger.getColumnsJson()))
        {
            throw new ServiceException("请至少配置一个台账列");
        }
        JSONArray arr;
        try
        {
            arr = JSON.parseArray(ledger.getColumnsJson());
        }
        catch (Exception e)
        {
            throw new ServiceException("列配置格式不正确");
        }
        if (arr == null || arr.isEmpty())
        {
            throw new ServiceException("请至少配置一个台账列");
        }
        Set<String> tableColumns = resolveTableColumns(ledger.getTableName());
        Set<String> used = new HashSet<>();
        JSONArray normalized = new JSONArray();
        for (int i = 0; i < arr.size(); i++)
        {
            JSONObject item = arr.getJSONObject(i);
            String field = item.getString("field");
            String source = StringUtils.isEmpty(item.getString("source")) ? "main" : item.getString("source");
            if (StringUtils.isEmpty(field) || !FIELD_PATTERN.matcher(field).matches())
            {
                throw new ServiceException("第" + (i + 1) + "列字段名不合法");
            }
            if (!"sys".equals(source) && !"main".equals(source))
            {
                throw new ServiceException("列「" + field + "」来源不合法");
            }
            if ("sys".equals(source) && !SYS_FIELDS.containsKey(field))
            {
                throw new ServiceException("不支持的系统字段：" + field);
            }
            if ("main".equals(source) && !tableColumns.contains(field))
            {
                throw new ServiceException("数据表中不存在字段：" + field);
            }
            if (!used.add(source + ":" + field))
            {
                continue;
            }
            JSONObject col = new JSONObject();
            col.put("field", field);
            col.put("source", source);
            col.put("label", StringUtils.isEmpty(item.getString("label"))
                    ? ("sys".equals(source) ? SYS_LABELS.get(field) : field) : item.getString("label"));
            col.put("width", item.getIntValue("width") > 0 ? item.getIntValue("width") : 140);
            col.put("visible", item.get("visible") == null || item.getBooleanValue("visible"));
            col.put("sortable", item.getBooleanValue("sortable"));
            col.put("filterable", item.getBooleanValue("filterable"));
            col.put("align", StringUtils.isEmpty(item.getString("align")) ? "left" : item.getString("align"));
            col.put("format", item.getString("format"));
            normalized.add(col);
        }
        ledger.setColumnsJson(normalized.toJSONString());
    }

    // ============================================================
    // 后台：授权
    // ============================================================

    @Override
    public List<FlowLedgerGrant> selectGrantList(Long ledgerId)
    {
        FlowLedgerGrant q = new FlowLedgerGrant();
        q.setLedgerId(ledgerId);
        return ledgerMapper.selectGrantList(q);
    }

    @Override
    @Transactional
    public int insertGrant(FlowLedgerGrant grant)
    {
        validateGrant(grant);
        grant.setCreateBy(SecurityUtils.getUsername());
        grant.setCreateTime(DateUtils.getNowDate());
        return ledgerMapper.insertGrant(grant);
    }

    @Override
    @Transactional
    public int updateGrant(FlowLedgerGrant grant)
    {
        validateGrant(grant);
        grant.setUpdateBy(SecurityUtils.getUsername());
        grant.setUpdateTime(DateUtils.getNowDate());
        return ledgerMapper.updateGrant(grant);
    }

    @Override
    @Transactional
    public int deleteGrantByIds(Long[] grantIds)
    {
        return ledgerMapper.deleteGrantByIds(grantIds);
    }

    private void validateGrant(FlowLedgerGrant grant)
    {
        if (grant.getLedgerId() == null)
        {
            throw new ServiceException("台账ID不能为空");
        }
        if (!GRANT_TYPES.contains(grant.getGrantType()))
        {
            throw new ServiceException("授权对象类型不合法");
        }
        if (!"all".equals(grant.getGrantType()) && grant.getTargetId() == null)
        {
            throw new ServiceException("请选择授权对象");
        }
        if (StringUtils.isEmpty(grant.getScopeType()) || !SCOPE_TYPES.contains(grant.getScopeType()))
        {
            throw new ServiceException("数据范围类型不合法");
        }
        if ("customDept".equals(grant.getScopeType()) || "customUser".equals(grant.getScopeType()))
        {
            if (StringUtils.isEmpty(grant.getScopeJson()))
            {
                throw new ServiceException("自定义范围不能为空");
            }
            try
            {
                JSON.parseObject(grant.getScopeJson());
            }
            catch (Exception e)
            {
                throw new ServiceException("自定义范围格式不正确");
            }
        }
        else
        {
            grant.setScopeJson(null);
        }
        if (StringUtils.isEmpty(grant.getCanExport()))
        {
            grant.setCanExport("0");
        }
    }

    // ============================================================
    // 后台：字段候选
    // ============================================================

    @Override
    public List<Map<String, Object>> selectLedgerFields(Long formId)
    {
        FlowForm form = formMapper.selectFlowFormByFormId(formId);
        if (form == null)
        {
            throw new ServiceException("流程表单不存在");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, String> e : SYS_LABELS.entrySet())
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("field", e.getKey());
            item.put("label", e.getValue());
            item.put("source", "sys");
            item.put("type", e.getKey().endsWith("Time") ? "datetime" : "text");
            list.add(item);
        }
        if (StringUtils.isEmpty(form.getTableName()))
        {
            return list;
        }
        // 业务字段：以数据表真实列为准，显示名优先取表单字段设计的中文名
        Map<String, String> labelMap = formFieldLabels(form.getFieldsJson());
        Map<String, String> formMap = formFieldForms(form.getFieldsJson());
        List<Map<String, Object>> cols = jdbcTemplate.query(
                "SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION",
                (rs, rowNum) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", rs.getString("COLUMN_NAME"));
                    m.put("comment", rs.getString("COLUMN_COMMENT"));
                    return m;
                }, form.getTableName());
        for (Map<String, Object> c : cols)
        {
            String name = String.valueOf(c.get("name"));
            if (INTERNAL_COLUMNS.contains(name))
            {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("field", name);
            item.put("label", StringUtils.isNotEmpty(labelMap.get(name)) ? labelMap.get(name)
                    : (StringUtils.isNotEmpty(String.valueOf(c.get("comment"))) ? String.valueOf(c.get("comment")) : name));
            item.put("source", "main");
            item.put("type", columnFormatOf(formMap.get(name)));
            list.add(item);
        }
        return list;
    }

    /** 表单字段的渲染类型（form，如 date / time / datetime），用于台账列默认显示格式 */
    private Map<String, String> formFieldForms(String fieldsJson)
    {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(fieldsJson))
        {
            return map;
        }
        try
        {
            JSONArray arr = JSON.parseArray(fieldsJson);
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject f = arr.getJSONObject(i);
                if (f.getString("field") != null)
                {
                    map.put(f.getString("field"), StringUtils.isEmpty(f.getString("form")) ? f.getString("dtype") : f.getString("form"));
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return map;
    }

    /** 台账列默认显示格式：时间框 → time，日期框 → date，日期时间框 → datetime，其余按文本 */
    private String columnFormatOf(String form)
    {
        if ("time".equals(form) || "date".equals(form) || "datetime".equals(form))
        {
            return form;
        }
        return "text";
    }

    private Map<String, String> formFieldLabels(String fieldsJson)
    {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(fieldsJson))
        {
            return map;
        }
        try
        {
            JSONArray arr = JSON.parseArray(fieldsJson);
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject f = arr.getJSONObject(i);
                if (f.getString("field") != null)
                {
                    map.put(f.getString("field"), f.getString("label"));
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return map;
    }

    // ============================================================
    // 前台：可见台账与台账数据
    // ============================================================

    @Override
    public List<FlowLedger> selectMyLedgers()
    {
        UserCtx ctx = currentUser();
        List<FlowLedger> all = ledgerMapper.selectActiveLedgerList();
        if (all.isEmpty())
        {
            return all;
        }
        List<FlowLedger> result = new ArrayList<>();
        if (ctx.admin)
        {
            for (FlowLedger l : all)
            {
                l.getParams().put("canExport", "1".equals(l.getExportFlag()));
                l.setColumnsJson(null);
                l.setQueryJson(null);
                result.add(l);
            }
            return result;
        }
        FlowLedgerGrant gq = new FlowLedgerGrant();
        List<FlowLedgerGrant> grants = ledgerMapper.selectGrantList(gq);
        Map<Long, List<FlowLedgerGrant>> byLedger = new HashMap<>();
        for (FlowLedgerGrant g : grants)
        {
            byLedger.computeIfAbsent(g.getLedgerId(), k -> new ArrayList<>()).add(g);
        }
        for (FlowLedger l : all)
        {
            List<FlowLedgerGrant> matched = matchedGrants(byLedger.get(l.getLedgerId()), ctx);
            if (matched.isEmpty())
            {
                continue;
            }
            l.getParams().put("canExport", "1".equals(l.getExportFlag())
                    && matched.stream().anyMatch(g -> "1".equals(g.getCanExport())));
            l.setColumnsJson(null);
            l.setQueryJson(null);
            result.add(l);
        }
        return result;
    }

    @Override
    public Map<String, Object> selectPortalConfig(Long ledgerId)
    {
        LedgerContext ctx = buildContext(ledgerId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ledgerId", ctx.ledger.getLedgerId());
        result.put("ledgerName", ctx.ledger.getLedgerName());
        result.put("ledgerDesc", ctx.ledger.getLedgerDesc());
        result.put("columns", columnMaps(ctx.columns));
        result.put("defaultSort", ctx.defaultSort);
        result.put("defaultSortOrder", ctx.defaultSortOrder);
        result.put("canExport", ctx.canExport);
        return result;
    }

    @Override
    public Map<String, Object> selectLedgerData(Long ledgerId, Map<String, String> params)
    {
        LedgerContext ctx = buildContext(ledgerId);
        int pageNum = parseInt(params.get("pageNum"), 1);
        int pageSize = parseInt(params.get("pageSize"), 10);
        if (pageSize <= 0 || pageSize > 500)
        {
            pageSize = 10;
        }
        QuerySql query = buildQuery(ctx, params, true);
        Long total = jdbcTemplate.queryForObject(query.countSql, Long.class, query.countParams.toArray());
        List<Map<String, Object>> rows = new ArrayList<>();
        if (total != null && total > 0)
        {
            List<Object> params2 = new ArrayList<>(query.dataParams);
            params2.add((pageNum - 1) * pageSize);
            params2.add(pageSize);
            rows = jdbcTemplate.queryForList(query.dataSql + " LIMIT ?, ?", params2.toArray());
            rows = mapRows(rows, ctx);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total == null ? 0L : total);
        result.put("rows", rows);
        result.put("statusCounts", loadStatusCounts(ctx, params));
        result.put("columns", columnMaps(ctx.visibleColumns()));
        result.put("canExport", ctx.canExport);
        result.put("ledgerName", ctx.ledger.getLedgerName());
        result.put("defaultSort", ctx.defaultSort);
        result.put("defaultSortOrder", ctx.defaultSortOrder);
        return result;
    }

    @Override
    public byte[] exportLedgerData(Long ledgerId, Map<String, String> params)
    {
        LedgerContext ctx = buildContext(ledgerId);
        if (!ctx.canExport)
        {
            throw new ServiceException("没有该台账的导出权限");
        }
        QuerySql query = buildQuery(ctx, params, false);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query.dataSql + " LIMIT 20000", query.dataParams.toArray());
        rows = mapRows(rows, ctx);
        List<ColumnDef> cols = ctx.visibleColumns();
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            Sheet sheet = wb.createSheet(ctx.ledger.getLedgerName());
            CellStyle headStyle = wb.createCellStyle();
            Font headFont = wb.createFont();
            headFont.setBold(true);
            headStyle.setFont(headFont);
            Row head = sheet.createRow(0);
            for (int i = 0; i < cols.size(); i++)
            {
                Cell cell = head.createCell(i);
                cell.setCellValue(cols.get(i).label);
                cell.setCellStyle(headStyle);
                sheet.setColumnWidth(i, Math.min(255, Math.max(12, cols.get(i).width / 8)) * 256);
            }
            for (int r = 0; r < rows.size(); r++)
            {
                Row row = sheet.createRow(r + 1);
                Map<String, Object> data = rows.get(r);
                for (int i = 0; i < cols.size(); i++)
                {
                    Object v = data.get(cols.get(i).field);
                    Cell cell = row.createCell(i);
                    if (v == null)
                    {
                        cell.setCellValue("");
                    }
                    else if (v instanceof Number)
                    {
                        cell.setCellValue(((Number) v).doubleValue());
                    }
                    else
                    {
                        cell.setCellValue(String.valueOf(v));
                    }
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
        catch (Exception e)
        {
            log.error("台账导出失败（ledgerId={}）：{}", ledgerId, e.getMessage(), e);
            throw new ServiceException("台账导出失败：" + e.getMessage());
        }
    }

    // ============================================================
    // 流程发起登记
    // ============================================================

    @Override
    public void registerInstanceExt(String instanceId, Long formId, String formKey, String processKey,
            String businessKey, String title, String startUserId, Long startDeptId)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            return;
        }
        try
        {
            jdbcTemplate.update(
                    "INSERT INTO flow_instance_ext (instance_id, form_id, form_key, process_key, business_key, "
                            + "title, start_user_id, start_dept_id, start_time, create_time) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) "
                            + "ON DUPLICATE KEY UPDATE title = VALUES(title), "
                            + "start_dept_id = IFNULL(start_dept_id, VALUES(start_dept_id))",
                    instanceId, formId, formKey, processKey, businessKey, title, startUserId, startDeptId);
        }
        catch (Exception e)
        {
            // 台账是旁路能力，登记失败不影响流程发起
            log.warn("流程台账实例快照登记失败（实例 {}）：{}", instanceId, e.getMessage());
        }
    }

    // ============================================================
    // 上下文构建与查询
    // ============================================================

    private LedgerContext buildContext(Long ledgerId)
    {
        FlowLedger ledger = ledgerMapper.selectFlowLedgerById(ledgerId);
        if (ledger == null)
        {
            throw new ServiceException("台账不存在或已删除");
        }
        if (!"0".equals(ledger.getStatus()))
        {
            throw new ServiceException("台账已停用");
        }
        if (StringUtils.isEmpty(ledger.getTableName()) || !TABLE_PATTERN.matcher(ledger.getTableName()).matches())
        {
            throw new ServiceException("台账数据表配置异常");
        }
        LedgerContext ctx = new LedgerContext();
        ctx.ledger = ledger;
        ctx.tableColumns = resolveTableColumns(ledger.getTableName());
        ctx.columns = parseColumns(ledger.getColumnsJson(), ctx.tableColumns);
        if (ctx.columns.isEmpty())
        {
            throw new ServiceException("台账未配置有效列，请联系管理员");
        }
        ctx.canExport = resolvePermission(ctx);
        if (!ctx.hasPermission)
        {
            throw new ServiceException("没有该台账的数据查看权限");
        }
        JSONObject q = null;
        if (StringUtils.isNotEmpty(ledger.getQueryJson()))
        {
            try
            {
                q = JSON.parseObject(ledger.getQueryJson());
            }
            catch (Exception ignored)
            {
            }
        }
        ctx.defaultSort = q == null ? "startTime" : q.getString("defaultSort");
        ctx.defaultSortOrder = q == null ? "desc" : q.getString("defaultSortOrder");
        if (StringUtils.isEmpty(ctx.defaultSort) || columnExpr(ctx, ctx.defaultSort) == null)
        {
            ctx.defaultSort = "startTime";
        }
        if (!"asc".equalsIgnoreCase(ctx.defaultSortOrder))
        {
            ctx.defaultSortOrder = "desc";
        }
        return ctx;
    }

    /**
     * 解析当前用户在该台账上的数据范围（多授权 OR 合并）
     *
     * @return 是否允许导出
     */
    private boolean resolvePermission(LedgerContext ctx)
    {
        UserCtx user = currentUser();
        if (user.admin)
        {
            ctx.hasPermission = true;
            ctx.permissionSql = null;
            return "1".equals(ctx.ledger.getExportFlag());
        }
        FlowLedgerGrant q = new FlowLedgerGrant();
        q.setLedgerId(ctx.ledger.getLedgerId());
        List<FlowLedgerGrant> matched = matchedGrants(ledgerMapper.selectGrantList(q), user);
        if (matched.isEmpty())
        {
            ctx.hasPermission = false;
            return false;
        }
        StringBuilder sql = new StringBuilder("(");
        List<Object> args = new ArrayList<>();
        boolean allScope = false;
        boolean first = true;
        for (FlowLedgerGrant g : matched)
        {
            String part = scopeSql(g, user, args);
            if (part == null)
            {
                allScope = true;
                break;
            }
            if (!first)
            {
                sql.append(" OR ");
            }
            sql.append("(").append(part).append(")");
            first = false;
        }
        ctx.hasPermission = true;
        if (allScope)
        {
            ctx.permissionSql = null;
            args.clear();
        }
        else
        {
            sql.append(")");
            ctx.permissionSql = sql.toString();
            ctx.permissionParams = args;
        }
        return "1".equals(ctx.ledger.getExportFlag())
                && matched.stream().anyMatch(g -> "1".equals(g.getCanExport()));
    }

    private List<FlowLedgerGrant> matchedGrants(List<FlowLedgerGrant> grants, UserCtx user)
    {
        List<FlowLedgerGrant> matched = new ArrayList<>();
        if (grants == null)
        {
            return matched;
        }
        for (FlowLedgerGrant g : grants)
        {
            if ("all".equals(g.getGrantType())
                    || ("user".equals(g.getGrantType()) && Objects.equals(g.getTargetId(), user.userId))
                    || ("role".equals(g.getGrantType()) && user.roleIds.contains(g.getTargetId()))
                    || ("dept".equals(g.getGrantType()) && (Objects.equals(g.getTargetId(), user.deptId)
                            || user.ancestorsContains(g.getTargetId())))
                    || ("company".equals(g.getGrantType()) && Objects.equals(g.getTargetId(), user.companyId)))
            {
                matched.add(g);
            }
        }
        return matched;
    }

    /** 单个授权的数据范围 SQL；返回 null 表示全部数据 */
    private String scopeSql(FlowLedgerGrant g, UserCtx user, List<Object> args)
    {
        String scope = g.getScopeType();
        if ("all".equals(scope))
        {
            return null;
        }
        if ("self".equals(scope))
        {
            args.add(String.valueOf(user.userId));
            return "p.START_USER_ID_ = ?";
        }
        if ("dept".equals(scope))
        {
            args.add(user.deptId);
            return DEPT_SQL + " = ?";
        }
        if ("deptAndChild".equals(scope))
        {
            args.add(user.deptId);
            args.add(String.valueOf(user.deptId));
            return "(" + DEPT_SQL + " = ? OR FIND_IN_SET(?, dept.ancestors))";
        }
        if ("company".equals(scope))
        {
            args.add(user.companyId);
            return DEPT_SQL + " = ?";
        }
        if ("companyAndChild".equals(scope))
        {
            args.add(user.companyId);
            args.add(String.valueOf(user.companyId));
            return "(" + DEPT_SQL + " = ? OR FIND_IN_SET(?, dept.ancestors))";
        }
        JSONObject json = null;
        try
        {
            json = StringUtils.isEmpty(g.getScopeJson()) ? null : JSON.parseObject(g.getScopeJson());
        }
        catch (Exception ignored)
        {
        }
        if ("customDept".equals(scope))
        {
            List<Long> deptIds = json == null ? Collections.emptyList() : json.getJSONArray("deptIds") == null
                    ? Collections.emptyList() : json.getJSONArray("deptIds").toJavaList(Long.class);
            if (deptIds == null || deptIds.isEmpty())
            {
                return "1 = 0";
            }
            boolean includeChild = json.getBooleanValue("includeChild");
            StringBuilder sb = new StringBuilder(DEPT_SQL).append(" IN (");
            for (int i = 0; i < deptIds.size(); i++)
            {
                if (i > 0)
                {
                    sb.append(", ");
                }
                sb.append("?");
                args.add(deptIds.get(i));
            }
            sb.append(")");
            if (!includeChild)
            {
                return sb.toString();
            }
            StringBuilder child = new StringBuilder("(").append(sb).append(" OR ");
            for (int i = 0; i < deptIds.size(); i++)
            {
                if (i > 0)
                {
                    child.append(" OR ");
                }
                child.append("FIND_IN_SET(?, dept.ancestors)");
                args.add(String.valueOf(deptIds.get(i)));
            }
            return child.append(")").toString();
        }
        if ("customUser".equals(scope))
        {
            List<Long> userIds = json == null ? Collections.emptyList() : json.getJSONArray("userIds") == null
                    ? Collections.emptyList() : json.getJSONArray("userIds").toJavaList(Long.class);
            if (userIds == null || userIds.isEmpty())
            {
                return "1 = 0";
            }
            StringBuilder sb = new StringBuilder("p.START_USER_ID_ IN (");
            for (int i = 0; i < userIds.size(); i++)
            {
                if (i > 0)
                {
                    sb.append(", ");
                }
                sb.append("?");
                args.add(String.valueOf(userIds.get(i)));
            }
            return sb.append(")").toString();
        }
        return "1 = 0";
    }

    /**
     * 构建查询 SQL（数据查询与导出共用）
     *
     * @param paged true=返回分页查询（不含 LIMIT，由调用方拼接）；false=导出
     */
    private QuerySql buildQuery(LedgerContext ctx, Map<String, String> params, boolean paged)
    {
        return buildQuery(ctx, params, paged, true);
    }

    /** 状态卡片统计：筛选条件与列表一致，但不叠加当前选中的流程状态 */
    private Map<String, Object> loadStatusCounts(LedgerContext ctx, Map<String, String> params)
    {
        Map<String, Object> counts = new LinkedHashMap<>();
        long all = 0L;
        Map<String, Long> byStatus = new HashMap<>();
        try
        {
            QuerySql statsQuery = buildQuery(ctx, params, false, false);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(statsQuery.statusCountSql, statsQuery.statusCountParams.toArray());
            for (Map<String, Object> row : rows)
            {
                String key = row.get("status_key") == null ? "" : String.valueOf(row.get("status_key"));
                Number cnt = (Number) row.get("status_count");
                long value = cnt == null ? 0L : cnt.longValue();
                byStatus.put(key, value);
                all += value;
            }
        }
        catch (Exception e)
        {
            log.warn("台账[{}]状态统计失败：{}", ctx.ledger.getLedgerKey(), e.getMessage());
        }
        counts.put("all", all);
        counts.put("running", byStatus.getOrDefault("running", 0L));
        counts.put("completed", byStatus.getOrDefault("completed", 0L));
        counts.put("canceled", byStatus.getOrDefault("canceled", 0L));
        return counts;
    }

    private QuerySql buildQuery(LedgerContext ctx, Map<String, String> params, boolean paged, boolean includeStatus)
    {
        // 引擎表与业务表可能使用不同字符集/排序规则（utf8mb3_bin / utf8mb4_general_ci 等），
        // 关联时统一 CONVERT 到「引擎流程实例ID列」的实际字符集再 COLLATE，
        // 避免 Illegal mix of collations，以及把 utf8mb4 排序规则强加到 utf8mb3 列时
        // 报 COLLATION 'utf8mb4_general_ci' is not valid for CHARACTER SET 'utf8mb3'
        String[] engineCharset = resolveInstanceCollation();
        String instanceRef;
        String engineIdRef;
        String extIdRef;
        String requestIdRef;
        if (engineCharset == null)
        {
            // 探测失败（引擎表缺失/无权限）时沿用历史写法，兼容 utf8mb4 库
            instanceRef = "CONVERT(d.process_instance_id USING utf8mb4) COLLATE utf8mb4_general_ci";
            engineIdRef = "p.ID_ COLLATE utf8mb4_general_ci";
            extIdRef = "e.instance_id COLLATE utf8mb4_general_ci";
            requestIdRef = "r.instance_id COLLATE utf8mb4_general_ci";
        }
        else
        {
            // 引擎侧不做 CONVERT，保持 p.ID_ 原样以保留主键索引；其余三侧转成引擎字符集
            String engineCollation = " COLLATE " + engineCharset[1];
            instanceRef = "CONVERT(d.process_instance_id USING " + engineCharset[0] + ")" + engineCollation;
            engineIdRef = "p.ID_";
            extIdRef = "CONVERT(e.instance_id USING " + engineCharset[0] + ")" + engineCollation;
            requestIdRef = "CONVERT(r.instance_id USING " + engineCharset[0] + ")" + engineCollation;
        }
        String from = " FROM `" + ctx.ledger.getTableName() + "` d"
                + " LEFT JOIN ACT_HI_PROCINST p ON " + engineIdRef + " = " + instanceRef
                + " LEFT JOIN flow_instance_ext e ON " + extIdRef + " = " + instanceRef
                + " LEFT JOIN sys_user u ON u.user_id = CAST(p.START_USER_ID_ AS UNSIGNED)"
                + " LEFT JOIN sys_dept dept ON dept.dept_id = " + DEPT_SQL
                + " LEFT JOIN flow_request r ON " + requestIdRef + " = " + instanceRef;
        // 仅统计有效流程数据：业务行必须关联到流程实例（实例被删除/清空的历史脏数据不进入台账）
        StringBuilder where = new StringBuilder(" WHERE d.process_instance_id IS NOT NULL AND p.ID_ IS NOT NULL");
        List<Object> args = new ArrayList<>();
        if (ctx.permissionSql != null)
        {
            where.append(" AND ").append(ctx.permissionSql);
            args.addAll(ctx.permissionParams);
        }
        String status = params.get("status");
        if (includeStatus && StringUtils.isNotEmpty(status))
        {
            where.append(" AND (").append(STATUS_SQL).append(") = ?");
            args.add(status);
        }
        String beginTime = params.get("beginTime");
        if (StringUtils.isNotEmpty(beginTime))
        {
            where.append(" AND p.START_TIME_ >= ?");
            args.add(beginTime + " 00:00:00");
        }
        String endTime = params.get("endTime");
        if (StringUtils.isNotEmpty(endTime))
        {
            where.append(" AND p.START_TIME_ <= ?");
            args.add(endTime + " 23:59:59");
        }
        String keyword = params.get("keyword");
        // 勾选导出：仅导出指定的流程实例
        String instanceIds = params.get("instanceIds");
        if (StringUtils.isNotEmpty(instanceIds))
        {
            List<String> ids = new ArrayList<>();
            for (String id : instanceIds.split(","))
            {
                if (StringUtils.isNotEmpty(id.trim()))
                {
                    ids.add(id.trim());
                }
            }
            if (!ids.isEmpty())
            {
                where.append(" AND d.process_instance_id IN (")
                        .append(String.join(",", Collections.nCopies(ids.size(), "?")))
                        .append(")");
                args.addAll(ids);
            }
        }
        if (StringUtils.isNotEmpty(keyword))
        {
            List<String> ors = new ArrayList<>();
            if (hasColumn(ctx, "sys", "title"))
            {
                ors.add("p.NAME_ LIKE ?");
                args.add("%" + keyword + "%");
            }
            if (hasColumn(ctx, "sys", "processNo") && ctx.tableColumns.contains("processNo"))
            {
                ors.add("d.`processNo` LIKE ?");
                args.add("%" + keyword + "%");
            }
            if (hasColumn(ctx, "sys", "startUserName"))
            {
                ors.add("u.nick_name LIKE ?");
                args.add("%" + keyword + "%");
            }
            for (ColumnDef c : ctx.columns)
            {
                if ("main".equals(c.source) && c.filterable && ctx.tableColumns.contains(c.field))
                {
                    ors.add("d.`" + c.field + "` LIKE ?");
                    args.add("%" + keyword + "%");
                }
            }
            if (!ors.isEmpty())
            {
                where.append(" AND (").append(String.join(" OR ", ors)).append(")");
            }
        }
        // 列级筛选：f_<field>
        for (ColumnDef c : ctx.columns)
        {
            String v = params.get("f_" + c.field);
            if (StringUtils.isEmpty(v))
            {
                continue;
            }
            String expr = columnExpr(ctx, c.field);
            if (expr == null)
            {
                continue;
            }
            if ("money".equals(c.format) || "number".equals(c.format))
            {
                where.append(" AND ").append(expr).append(" = ?");
                args.add(v);
            }
            else
            {
                where.append(" AND ").append(expr).append(" LIKE ?");
                args.add("%" + v + "%");
            }
        }
        String orderBy = params.get("orderByColumn");
        String isAsc = params.get("isAsc");
        String sortField = StringUtils.isNotEmpty(orderBy) ? orderBy : ctx.defaultSort;
        String sortOrder = StringUtils.isNotEmpty(isAsc) ? ("ascending".equalsIgnoreCase(isAsc) ? "asc" : "desc")
                : ctx.defaultSortOrder;
        String sortExpr = columnExpr(ctx, sortField);
        if (sortExpr == null || !isSortable(ctx, sortField))
        {
            sortExpr = "p.START_TIME_";
            sortOrder = "desc";
        }
        StringBuilder select = new StringBuilder("SELECT d.id AS businessKey, d.process_instance_id AS instanceId");
        for (int i = 0; i < ctx.columns.size(); i++)
        {
            String expr = columnExpr(ctx, ctx.columns.get(i).field);
            select.append(", ").append(expr == null ? "NULL" : expr).append(" AS v").append(i);
        }
        QuerySql q = new QuerySql();
        q.dataSql = select + from + where + " ORDER BY " + sortExpr + " " + sortOrder;
        q.dataParams = args;
        q.countSql = "SELECT COUNT(*)" + from + where;
        q.countParams = new ArrayList<>(args);
        if (!includeStatus)
        {
            q.statusCountSql = "SELECT (" + STATUS_SQL + ") AS status_key, COUNT(*) AS status_count"
                    + from + where + " GROUP BY (" + STATUS_SQL + ")";
            q.statusCountParams = new ArrayList<>(args);
        }
        return q;
    }

    private List<Map<String, Object>> mapRows(List<Map<String, Object>> rows, LedgerContext ctx)
    {
        List<Map<String, Object>> list = new ArrayList<>(rows.size());
        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        for (Map<String, Object> row : rows)
        {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("businessKey", row.get("businessKey"));
            out.put("instanceId", row.get("instanceId"));
            for (int i = 0; i < ctx.columns.size(); i++)
            {
                ColumnDef c = ctx.columns.get(i);
                Object v = row.get("v" + i);
                if (v instanceof Timestamp)
                {
                    v = "date".equals(c.format) ? date.format((Timestamp) v)
                            : "time".equals(c.format) ? time.format((Timestamp) v) : datetime.format((Timestamp) v);
                }
                else if (v instanceof java.sql.Date)
                {
                    v = date.format((java.sql.Date) v);
                }
                else if (v instanceof java.time.LocalDateTime)
                {
                    v = "date".equals(c.format)
                            ? ((java.time.LocalDateTime) v).toLocalDate().toString()
                            : "time".equals(c.format)
                                    ? ((java.time.LocalDateTime) v).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
                                    : ((java.time.LocalDateTime) v).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                else if (v instanceof java.time.LocalDate)
                {
                    v = v.toString();
                }
                else if (v instanceof java.time.LocalTime)
                {
                    v = ((java.time.LocalTime) v).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                }
                else if (v instanceof java.sql.Time)
                {
                    v = time.format((java.sql.Time) v);
                }
                else if (v instanceof BigDecimal && ("money".equals(c.format) || "number".equals(c.format)))
                {
                    v = ((BigDecimal) v).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
                }
                out.put(c.field, v);
            }
            list.add(out);
        }
        return list;
    }

    private boolean hasColumn(LedgerContext ctx, String source, String field)
    {
        return ctx.columns.stream().anyMatch(c -> source.equals(c.source) && field.equals(c.field));
    }

    private boolean isSortable(LedgerContext ctx, String field)
    {
        return ctx.columns.stream().anyMatch(c -> field.equals(c.field) && c.sortable);
    }

    /** 字段 → SQL 表达式（严格白名单，未命中返回 null） */
    private String columnExpr(LedgerContext ctx, String field)
    {
        for (ColumnDef c : ctx.columns)
        {
            if (!field.equals(c.field))
            {
                continue;
            }
            if ("sys".equals(c.source))
            {
                String expr = SYS_FIELDS.get(field);
                // 表单未生成 processNo 列时降级为空，避免整页查询报错
                if ("processNo".equals(field) && !ctx.tableColumns.contains("processNo"))
                {
                    return "NULL";
                }
                return expr;
            }
            if (FIELD_PATTERN.matcher(field).matches() && ctx.tableColumns.contains(field))
            {
                return "d.`" + field + "`";
            }
        }
        return null;
    }

    private List<ColumnDef> parseColumns(String columnsJson, Set<String> tableColumns)
    {
        List<ColumnDef> list = new ArrayList<>();
        if (StringUtils.isEmpty(columnsJson))
        {
            return list;
        }
        try
        {
            JSONArray arr = JSON.parseArray(columnsJson);
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject o = arr.getJSONObject(i);
                ColumnDef c = new ColumnDef();
                c.field = o.getString("field");
                c.source = StringUtils.isEmpty(o.getString("source")) ? "main" : o.getString("source");
                c.label = o.getString("label");
                c.width = o.getIntValue("width") > 0 ? o.getIntValue("width") : 140;
                c.visible = o.get("visible") == null || o.getBooleanValue("visible");
                c.sortable = o.getBooleanValue("sortable");
                c.filterable = o.getBooleanValue("filterable");
                c.align = StringUtils.isEmpty(o.getString("align")) ? "left" : o.getString("align");
                c.format = o.getString("format");
                if (StringUtils.isEmpty(c.field) || !FIELD_PATTERN.matcher(c.field).matches())
                {
                    continue;
                }
                if ("sys".equals(c.source))
                {
                    if (!SYS_FIELDS.containsKey(c.field))
                    {
                        continue;
                    }
                }
                else if (!tableColumns.contains(c.field))
                {
                    continue;
                }
                list.add(c);
            }
        }
        catch (Exception e)
        {
            log.warn("台账列配置解析失败：{}", e.getMessage());
        }
        return list;
    }

    private List<Map<String, Object>> columnMaps(List<ColumnDef> columns)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ColumnDef c : columns)
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("field", c.field);
            m.put("label", c.label);
            m.put("source", c.source);
            m.put("width", c.width);
            m.put("visible", c.visible);
            m.put("sortable", c.sortable);
            m.put("filterable", c.filterable);
            m.put("align", c.align);
            m.put("format", c.format);
            list.add(m);
        }
        return list;
    }

    /**
     * 探测 Flowable 引擎表流程实例ID列的实际字符集与排序规则。
     *
     * <p>历史部署的引擎表可能由 Flowable 自动建表时为 utf8mb3/utf8_bin（MySQL 8 下
     * 显示为 utf8mb3），而业务表为 utf8mb4_general_ci。JOIN 时若把 utf8mb4 的排序规则
     * 强加到 utf8mb3 列上，MySQL 会直接报
     * "COLLATION 'utf8mb4_general_ci' is not valid for CHARACTER SET 'utf8mb3'"。
     * 因此按库的实际定义动态生成 CONVERT/COLLATE 片段。
     *
     * @return {字符集, 排序规则}；无法探测时返回 null，由调用方走兼容默认值
     */
    private String[] resolveInstanceCollation()
    {
        long now = System.currentTimeMillis();
        String[] cached = INSTANCE_COLLATION_CACHE;
        if (cached != null && INSTANCE_COLLATION_EXPIRE_AT > now)
        {
            return cached;
        }
        String[] result = null;
        try
        {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT CHARACTER_SET_NAME, COLLATION_NAME FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ACT_HI_PROCINST' "
                            + "AND COLUMN_NAME = 'ID_'");
            if (!rows.isEmpty())
            {
                Map<String, Object> row = rows.get(0);
                String charset = row.get("CHARACTER_SET_NAME") == null ? null
                        : String.valueOf(row.get("CHARACTER_SET_NAME"));
                String collation = row.get("COLLATION_NAME") == null ? null
                        : String.valueOf(row.get("COLLATION_NAME"));
                // 只接受白名单内的字符集/排序规则格式，避免拼接非法值
                if (charset != null && collation != null && INSTANCE_CHARSETS.contains(charset)
                        && COLLATION_PATTERN.matcher(collation).matches())
                {
                    result = new String[] { charset, collation };
                }
            }
        }
        catch (Exception e)
        {
            log.warn("探测引擎表字符集失败，回退 utf8mb4_general_ci：{}", e.getMessage());
        }
        INSTANCE_COLLATION_CACHE = result;
        INSTANCE_COLLATION_EXPIRE_AT = now + INSTANCE_COLLATION_CACHE_TTL_MS;
        return result;
    }

    private Set<String> resolveTableColumns(String tableName)
    {
        if (StringUtils.isEmpty(tableName) || !TABLE_PATTERN.matcher(tableName).matches())
        {
            return Collections.emptySet();
        }
        long now = System.currentTimeMillis();
        TableColumnCache cached = TABLE_COLUMN_CACHE.get(tableName);
        if (cached != null && cached.expireAt > now)
        {
            return cached.columns;
        }
        Set<String> set = new HashSet<>();
        try
        {
            List<String> cols = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                    String.class, tableName);
            set.addAll(cols);
        }
        catch (Exception e)
        {
            log.warn("读取数据表列失败（{}）：{}", tableName, e.getMessage());
        }
        TABLE_COLUMN_CACHE.put(tableName, new TableColumnCache(now + TABLE_COLUMN_CACHE_TTL_MS, set));
        return set;
    }

    // ============================================================
    // 当前用户上下文
    // ============================================================

    private UserCtx currentUser()
    {
        UserCtx ctx = new UserCtx();
        ctx.userId = SecurityUtils.getUserId();
        ctx.deptId = SecurityUtils.getDeptId();
        try
        {
            com.ruoyi.common.core.domain.entity.SysUser user = SecurityUtils.getLoginUser().getUser();
            ctx.admin = user != null && user.isAdmin();
            if (user != null && user.getRoles() != null)
            {
                for (com.ruoyi.common.core.domain.entity.SysRole role : user.getRoles())
                {
                    ctx.roleIds.add(role.getRoleId());
                }
            }
        }
        catch (Exception ignored)
        {
        }
        if (ctx.deptId != null)
        {
            try
            {
                List<String> ancestors = jdbcTemplate.queryForList(
                        "SELECT ancestors FROM sys_dept WHERE dept_id = ?", String.class, ctx.deptId);
                ctx.ancestors = ancestors.isEmpty() ? "" : ancestors.get(0);
            }
            catch (Exception ignored)
            {
            }
        }
        ctx.companyId = resolveCompanyId(ctx.ancestors, ctx.deptId);
        return ctx;
    }

    /** 公司节点 = 部门链路上的一级节点（ancestors 中第一个非 0 部门） */
    private Long resolveCompanyId(String ancestors, Long deptId)
    {
        if (StringUtils.isEmpty(ancestors))
        {
            return deptId;
        }
        for (String s : ancestors.split(","))
        {
            if (StringUtils.isNotEmpty(s) && !"0".equals(s.trim()))
            {
                try
                {
                    return Long.valueOf(s.trim());
                }
                catch (NumberFormatException ignored)
                {
                }
            }
        }
        return deptId;
    }

    private int parseInt(String v, int def)
    {
        try
        {
            return v == null ? def : Integer.parseInt(v);
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }

    // ============================================================
    // 内部结构
    // ============================================================

    private static class ColumnDef
    {
        String field;
        String label;
        String source;
        String align;
        String format;
        int width;
        boolean visible;
        boolean sortable;
        boolean filterable;
    }

    private static class LedgerContext
    {
        FlowLedger ledger;
        List<ColumnDef> columns = new ArrayList<>();
        Set<String> tableColumns = Collections.emptySet();
        String permissionSql;
        List<Object> permissionParams = new ArrayList<>();
        boolean hasPermission;
        boolean canExport;
        String defaultSort = "startTime";
        String defaultSortOrder = "desc";

        List<ColumnDef> visibleColumns()
        {
            List<ColumnDef> list = new ArrayList<>();
            for (ColumnDef c : columns)
            {
                if (c.visible)
                {
                    list.add(c);
                }
            }
            return list;
        }
    }

    private static class QuerySql
    {
        String dataSql;
        List<Object> dataParams;
        String countSql;
        List<Object> countParams;
        String statusCountSql;
        List<Object> statusCountParams;
    }

    private static class UserCtx
    {
        Long userId;
        Long deptId;
        Long companyId;
        String ancestors = "";
        boolean admin;
        Set<Long> roleIds = new HashSet<>();

        boolean ancestorsContains(Long deptId)
        {
            if (deptId == null || ancestors == null || ancestors.isEmpty())
            {
                return false;
            }
            for (String s : ancestors.split(","))
            {
                if (s.trim().equals(String.valueOf(deptId)))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
