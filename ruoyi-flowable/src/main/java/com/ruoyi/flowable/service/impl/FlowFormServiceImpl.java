package com.ruoyi.flowable.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.config.FlowSystemFields;
import com.ruoyi.flowable.domain.FlowForm;
import com.ruoyi.flowable.mapper.FlowFormMapper;
import com.ruoyi.flowable.service.FlowProcessNoService;
import com.ruoyi.flowable.service.IFlowFormService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 流程表单 Service 实现（建模引擎）
 *
 * @author ruoyi
 */
@Service
public class FlowFormServiceImpl implements IFlowFormService
{
    private static final Logger log = LoggerFactory.getLogger(FlowFormServiceImpl.class);

    @Autowired
    private FlowFormMapper flowFormMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.flowable.engine.RepositoryService repositoryService;

    @Autowired
    private org.flowable.engine.RuntimeService runtimeService;

    @Autowired
    private org.flowable.engine.TaskService taskService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private com.ruoyi.flowable.service.IFlowUrgencyLevelService urgencyLevelService;

    /** 流程编号生成（按流程高级设置里的编号规则） */
    @Autowired
    private FlowProcessNoService processNoService;

    /** 字段类型 → 数据库类型（由 columnType(JSONObject) 按表现形式/类型/长度动态推导） */

    @Override
    public List<FlowForm> selectFlowFormList(FlowForm form)
    {
        return flowFormMapper.selectFlowFormList(form);
    }

    @Override
    public FlowForm selectFlowFormByFormId(Long formId)
    {
        return flowFormMapper.selectFlowFormByFormId(formId);
    }

    @Override
    public FlowForm selectFlowFormByProcessKey(String processKey)
    {
        // 优先：流程模型绑定的表单（模型 metaInfo.formId，一个表单可被多个模型关联）
        org.flowable.engine.repository.Model model = null;
        for (org.flowable.engine.repository.Model m : repositoryService.createModelQuery().modelKey(processKey).list())
        {
            if (model == null || (m.getVersion() != null && model.getVersion() != null && m.getVersion() > model.getVersion()))
            {
                model = m;
            }
        }
        if (model != null && StringUtils.isNotEmpty(model.getMetaInfo()))
        {
            try
            {
                Long formId = com.alibaba.fastjson2.JSONObject.parseObject(model.getMetaInfo()).getLong("formId");
                if (formId != null)
                {
                    FlowForm bound = flowFormMapper.selectFlowFormByFormId(formId);
                    if (bound != null)
                    {
                        return bound;
                    }
                }
            }
            catch (Exception ignored)
            {
            }
        }
        // 兼容旧数据：表单侧绑定的 process_key
        return flowFormMapper.selectFlowFormByProcessKey(processKey);
    }

    @Override
    @Transactional
    public int insertFlowForm(FlowForm form)
    {
        // 表单Key：用户不填，使用自增主键ID作为唯一数字Key（表名 flow_data_{id} 简短）
        boolean autoKey = StringUtils.isEmpty(form.getFormKey());
        if (autoKey)
        {
            form.setFormKey("tmp_" + System.nanoTime());
        }
        else if (flowFormMapper.selectFlowFormByFormKey(form.getFormKey()) != null)
        {
            throw new ServiceException("表单Key已存在：" + form.getFormKey());
        }
        validateFields(form.getFieldsJson());
        SysUser user = userService.selectUserById(SecurityUtils.getUserId());
        form.setCreateBy(user.getUserName());
        int rows = flowFormMapper.insertFlowForm(form);
        if (autoKey && form.getFormId() != null)
        {
            form.setFormKey(String.valueOf(form.getFormId()));
            flowFormMapper.updateFlowForm(form);
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateFlowForm(FlowForm form)
    {
        validateFields(form.getFieldsJson());
        form.setUpdateBy(SecurityUtils.getUsername());
        return flowFormMapper.updateFlowForm(form);
    }

    @Override
    @Transactional
    public int deleteFlowFormByFormIds(Long[] formIds)
    {
        return flowFormMapper.deleteFlowFormByFormIds(formIds);
    }

    /** 表现形式 → 数据库列类型（按字段类型/长度/小数位推导） */
    private String columnType(JSONObject f)
    {
        String dtype = f.getString("dtype");
        if ("datetime".equals(dtype))
        {
            return "datetime";
        }
        if ("date".equals(dtype))
        {
            return "date";
        }
        if ("float".equals(dtype))
        {
            int scale = f.getIntValue("scale");
            return "decimal(18," + (scale > 0 ? scale : 2) + ")";
        }
        if ("int".equals(dtype))
        {
            return "int";
        }
        int len = f.getIntValue("length");
        if (len <= 0)
        {
            len = 500;
        }
        // 超长文本（多行文本等）落 text 类型，避免 varchar 上限
        if (len > 4000)
        {
            return "text";
        }
        return "varchar(" + len + ")";
    }

    /** 兼容旧字段结构：{type:text/textarea/number/date/select} → {form,dtype,length,scale} */
    private void normalizeField(JSONObject f)
    {
        if (f.containsKey("form"))
        {
            return;
        }
        String legacy = f.getString("type");
        String form = "text";
        String dtype = "text";
        if ("textarea".equals(legacy))
        {
            form = "textarea";
        }
        else if ("number".equals(legacy))
        {
            dtype = "float";
        }
        else if ("date".equals(legacy))
        {
            dtype = "date";
        }
        else if ("select".equals(legacy))
        {
            form = "select";
        }
        f.put("form", form);
        f.put("dtype", dtype);
        if (f.getIntValue("length") <= 0)
        {
            f.put("length", 500);
        }
        if (f.getIntValue("scale") <= 0)
        {
            f.put("scale", 2);
        }
        if (StringUtils.isEmpty(f.getString("table")))
        {
            f.put("table", "main");
        }
    }

    @Override
    @Transactional
    public String createTable(Long formId)
    {
        FlowForm form = flowFormMapper.selectFlowFormByFormId(formId);
        if (form == null)
        {
            throw new ServiceException("表单不存在");
        }
        String formKey = form.getFormKey();
        if (!formKey.matches("^[a-z0-9][a-z0-9_]{0,39}$"))
        {
            throw new ServiceException("表单Key仅允许小写字母开头的字母/数字/下划线（1-40位），当前：" + formKey);
        }
        List<JSONObject> fields = parseFields(form.getFieldsJson());
        for (JSONObject f : fields)
        {
            normalizeField(f);
        }
        // 按所属表分组（main / dt1 / dt2 ...，参照泛微e9明细表规则）
        java.util.Map<String, List<JSONObject>> groups = new java.util.LinkedHashMap<>();
        for (JSONObject f : fields)
        {
            String table = StringUtils.isEmpty(f.getString("table")) ? "main" : f.getString("table");
            if (!table.matches("^(main|dt[1-9])$"))
            {
                throw new ServiceException("非法的字段所属表标识：" + table);
            }
            groups.computeIfAbsent(table, k -> new ArrayList<>()).add(f);
        }
        String tableName = "flow_data_" + formKey;
        // 主表强制补齐系统内置字段（标题/紧急程度/签字意见/打印次数），保证物理表一定存在这几列
        List<JSONObject> mainFields = mergeSystemFieldsWithUrgencyOptions(groups.get("main"));
        createOrPatchTable(tableName, form.getFormName() + "流程数据", mainFields, false);
        // 明细表：flow_data_{key}_dtN，含 mainid 关联主表主键
        for (int i = 1; i <= 9; i++)
        {
            List<JSONObject> dtFields = groups.get("dt" + i);
            if (dtFields == null)
            {
                continue;
            }
            createOrPatchTable(tableName + "_dt" + i, form.getFormName() + "明细表" + i, dtFields, true);
        }
        // 回写表名 + 合并后的字段清单（系统字段落库，保证字段设计与物理表口径一致）
        List<JSONObject> merged = new ArrayList<>(mainFields);
        for (int i = 1; i <= 9; i++)
        {
            List<JSONObject> dtFields = groups.get("dt" + i);
            if (dtFields != null)
            {
                merged.addAll(dtFields);
            }
        }
        FlowForm update = new FlowForm();
        update.setFormId(formId);
        update.setTableName(tableName);
        update.setFieldsJson(JSON.toJSONString(merged));
        update.setUpdateBy(SecurityUtils.getUsername());
        flowFormMapper.updateFlowForm(update);
        return tableName;
    }

    /**
     * 合并系统字段，其中「紧急程度」的选项由 flow_urgency_level 配置表驱动。
     * 配置表无数据时回退 FlowSystemFields 内置默认档位，保证老数据兼容。
     */
    private List<JSONObject> mergeSystemFieldsWithUrgencyOptions(List<JSONObject> mainFields)
    {
        List<JSONObject> base = mainFields == null ? new ArrayList<>() : new ArrayList<>(mainFields);
        JSONArray urgencyOptions = buildUrgencyOptions();
        JSONArray defs = FlowSystemFields.definitions(urgencyOptions);
        // 先按系统定义刷新「已存在」的系统字段：紧急程度选项按配置表、文本长度只放宽不收窄
        for (JSONObject f : base)
        {
            String field = f.getString("field");
            JSONObject def = findDefinition(defs, field);
            if (def == null)
            {
                continue;
            }
            if (FlowSystemFields.FIELD_URGENCY.equals(field))
            {
                if (urgencyOptions != null)
                {
                    f.put("selectOptions", urgencyOptions);
                }
                continue;
            }
            // 系统定义升级（如签字意见 2000 → 4000）时同步到表单已保存的字段清单
            if (def.getIntValue("length") > f.getIntValue("length"))
            {
                f.put("length", def.getIntValue("length"));
            }
            if (StringUtils.isEmpty(f.getString("dtype")))
            {
                f.put("dtype", def.getString("dtype"));
            }
        }
        java.util.Set<String> exist = new java.util.HashSet<>();
        for (JSONObject f : base)
        {
            exist.add(f.getString("field"));
        }
        List<JSONObject> built = new ArrayList<>();
        for (int i = 0; i < defs.size(); i++)
        {
            JSONObject def = defs.getJSONObject(i);
            if (exist.contains(def.getString("field")))
            {
                continue;
            }
            built.add(def);
        }
        List<JSONObject> merged = new ArrayList<>(built);
        merged.addAll(base);
        int order = 0;
        for (JSONObject f : merged)
        {
            String table = f.getString("table");
            if (table == null || "main".equals(table))
            {
                f.put("order", ++order);
            }
        }
        return merged;
    }

    /** 从系统字段定义中按字段名取定义，未命中返回 null */
    private JSONObject findDefinition(JSONArray defs, String field)
    {
        if (defs == null || StringUtils.isEmpty(field))
        {
            return null;
        }
        for (int i = 0; i < defs.size(); i++)
        {
            JSONObject def = defs.getJSONObject(i);
            if (field.equals(def.getString("field")))
            {
                return def;
            }
        }
        return null;
    }

    /** 读取紧急程度可选档位（配置表 → [{label, value}]），无数据返回 null */
    private JSONArray buildUrgencyOptions()
    {
        try
        {
            List<com.ruoyi.flowable.domain.FlowUrgencyLevel> levels = urgencyLevelService.selectSelectableList();
            if (levels == null || levels.isEmpty())
            {
                return null;
            }
            JSONArray arr = new JSONArray();
            for (com.ruoyi.flowable.domain.FlowUrgencyLevel lv : levels)
            {
                JSONObject o = new JSONObject();
                o.put("label", lv.getDisplayName());
                o.put("value", lv.getLevelValue());
                arr.add(o);
            }
            return arr;
        }
        catch (Exception e)
        {
            // 配置表未初始化等情况：回退内置默认档位
            return null;
        }
    }

    /** 建表（不存在则建，存在则补齐缺失列）；isDetail=true 时含 mainid 关联字段 */
    private void createOrPatchTable(String tableName, String comment, List<JSONObject> fields, boolean isDetail)
    {
        if (fields == null || fields.isEmpty())
        {
            return;
        }
        java.util.LinkedHashSet<String> existing = new java.util.LinkedHashSet<>();
        // 现有列的文本容量：varchar(n) 记 n，text 记 -1（不限长），非文本列记 null
        java.util.Map<String, Long> textLen = new java.util.HashMap<>();
        boolean tableExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
            Long.class, tableName) > 0;
        if (tableExists)
        {
            List<java.util.Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name AS col, data_type AS dt, character_maximum_length AS maxlen "
                    + "FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ?",
                tableName);
            for (java.util.Map<String, Object> c : columns)
            {
                Object colObj = c.get("col");
                if (colObj == null)
                {
                    continue;
                }
                String col = String.valueOf(colObj);
                existing.add(col);
                Object dtObj = c.get("dt");
                String dataType = dtObj == null ? "" : String.valueOf(dtObj);
                Object maxObj = c.get("maxlen");
                Long len = null;
                if ("text".equalsIgnoreCase(dataType))
                {
                    len = -1L;
                }
                else if (maxObj instanceof Number)
                {
                    len = ((Number) maxObj).longValue();
                }
                textLen.put(col, len);
            }
        }
        if (!tableExists)
        {
            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE `").append(tableName).append("` (\n");
            ddl.append("  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n");
            if (isDetail)
            {
                ddl.append("  `mainid` bigint(20) NOT NULL COMMENT '主表主键ID',\n");
            }
            appendColumns(ddl, fields, existing);
            ddl.append("  PRIMARY KEY (`id`)");
            if (isDetail)
            {
                ddl.append(",\n  KEY `idx_mainid` (`mainid`)");
            }
            ddl.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='").append(comment).append("'");
            jdbcTemplate.execute(ddl.toString());
            if (!isDetail)
            {
                // 主表系统字段索引：标题快速检索 / 紧急程度过滤排序
                ensureSystemFieldIndexes(tableName, new java.util.LinkedHashSet<>(java.util.Arrays.asList(
                        FlowSystemFields.FIELD_TITLE, FlowSystemFields.FIELD_URGENCY, FlowSystemFields.FIELD_PROCESS_NO)));
            }
            return;
        }
        // 已存在：补齐缺失字段列
        for (JSONObject f : fields)
        {
            String col = f.getString("field");
            if (!existing.contains(col))
            {
                jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD COLUMN `" + col + "` "
                    + columnType(f) + " COMMENT '" + f.getString("label") + "'");
                existing.add(col);
            }
        }
        // 已存在：类型偏窄的文本列按最新字段定义放宽（如签字意见 varchar(2000) → varchar(4000)）
        for (JSONObject f : fields)
        {
            widenTextColumn(tableName, f, textLen);
        }
        if (!isDetail)
        {
            ensureSystemFieldIndexes(tableName, existing);
        }
    }

    /**
     * 已存在列的文本容量放宽（只放宽、不收窄）：
     * 目标为 text 时 varchar → text；目标为 varchar(n) 时仅当列上限小于 n 才 MODIFY。
     * 数值/日期等非文本列一律不动，避免误改用户手工调整过的列类型。
     *
     * @param tableName 物理表名
     * @param f 字段定义
     * @param textLen 现有文本列容量：varchar(n) → n，text → -1，非文本列 → null
     */
    private void widenTextColumn(String tableName, JSONObject f, java.util.Map<String, Long> textLen)
    {
        String col = f.getString("field");
        if (StringUtils.isEmpty(col) || textLen == null)
        {
            return;
        }
        Long cur = textLen.get(col);
        if (cur == null)
        {
            return;
        }
        String target = columnType(f);
        String comment = f.getString("label") == null ? "" : f.getString("label").replace("'", "''");
        try
        {
            if ("text".equals(target))
            {
                if (cur >= 0)
                {
                    jdbcTemplate.execute("ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + col
                        + "` text COMMENT '" + comment + "'");
                    textLen.put(col, -1L);
                }
                return;
            }
            if (!target.startsWith("varchar("))
            {
                return;
            }
            int targetLen = Integer.parseInt(target.substring("varchar(".length(), target.length() - 1));
            if (cur < 0 || cur >= targetLen)
            {
                return;
            }
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + col + "` " + target
                + " COMMENT '" + comment + "'");
            textLen.put(col, (long) targetLen);
        }
        catch (Exception e)
        {
            // 放宽失败不阻断建表（如列被索引占用），保留原列类型
            log.warn("放宽字段长度失败：{}.{} → {}", tableName, col, target, e);
        }
    }

    private void appendColumns(StringBuilder ddl, List<JSONObject> fields, java.util.Set<String> existing)
    {
        for (JSONObject f : fields)
        {
            String col = f.getString("field");
            if (existing.contains(col))
            {
                continue;
            }
            ddl.append("  `").append(col).append("` ").append(columnType(f)).append(" COMMENT '")
               .append(f.getString("label")).append("',\n");
        }
        ddl.append("  `process_instance_id` varchar(64) COMMENT '流程实例ID',\n");
        ddl.append("  `create_by` varchar(64) COMMENT '创建人',\n");
        ddl.append("  `create_time` datetime COMMENT '创建时间',\n");
    }

    /**
     * 主表系统字段索引：标题用于快速检索、紧急程度用于待办过滤/排序。
     * 列不存在时静默跳过（明细表 / 历史表结构）。
     */
    private void ensureSystemFieldIndexes(String tableName, java.util.Set<String> columns)
    {
        if (columns == null)
        {
            return;
        }
        if (columns.contains(FlowSystemFields.FIELD_TITLE))
        {
            createIndexIfAbsent(tableName, "idx_title", "`title`(191)");
        }
        if (columns.contains(FlowSystemFields.FIELD_URGENCY))
        {
            createIndexIfAbsent(tableName, "idx_urgency", "`urgency`");
        }
        if (columns.contains(FlowSystemFields.FIELD_PROCESS_NO))
        {
            createIndexIfAbsent(tableName, "idx_process_no", "`processNo`");
        }
    }

    private void createIndexIfAbsent(String tableName, String indexName, String indexCols)
    {
        Long cnt = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
            Long.class, tableName, indexName);
        if (cnt != null && cnt > 0)
        {
            return;
        }
        try
        {
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD INDEX `" + indexName + "` (" + indexCols + ")");
        }
        catch (Exception ignored)
        {
            // 建索引失败不阻断建表（如列类型不匹配），仅跳过
        }
    }

    @Override
    @Transactional
    public String launch(String definitionKey, Long formId, Map<String, Object> variables, Long approverUserId, String title)
    {
        org.flowable.engine.repository.ProcessDefinition definition = repositoryService
                .createProcessDefinitionQuery().processDefinitionKey(definitionKey).latestVersion().singleResult();
        if (definition == null)
        {
            throw new ServiceException("流程未部署或不存在：" + definitionKey);
        }
        if (definition.isSuspended())
        {
            throw new ServiceException("流程已挂起，无法发起");
        }
        Map<String, Object> vars = new java.util.HashMap<>();
        if (variables != null)
        {
            vars.putAll(variables);
        }
        if (approverUserId != null)
        {
            vars.put("approver", String.valueOf(approverUserId));
        }
        String businessKey = null;
        // 绑定表单：数据落表，businessKey=数据行ID
        if (formId != null)
        {
            FlowForm form = flowFormMapper.selectFlowFormByFormId(formId);
            if (form == null)
            {
                throw new ServiceException("表单不存在");
            }
            if (StringUtils.isEmpty(form.getTableName()))
            {
                throw new ServiceException("表单尚未生成数据表，请先在表单管理中执行“生成数据表”");
            }
            // 流程编号：按「流程 → 高级设置 → 流程编号规则」生成，必须早于插入数据行（编号要落库），
            // 同时写入流程变量，详情页/打印页回显与列表检索都用它
            String processNo = null;
            com.alibaba.fastjson2.JSONObject noRule = processNoService.ruleOf(definitionKey);
            // 规则允许手工修改时，优先用发起人填的号；与历史单重号则退回自动发号，避免两张同号
            if (noRule != null && noRule.getBooleanValue("allowEdit"))
            {
                Object userNo = vars.get(FlowSystemFields.FIELD_PROCESS_NO);
                if (userNo != null && StringUtils.isNotEmpty(String.valueOf(userNo).trim()))
                {
                    String candidate = String.valueOf(userNo).trim();
                    if (!processNoExists(form.getTableName(), candidate))
                    {
                        processNo = candidate;
                    }
                    else
                    {
                        log.warn("手工填写的流程编号 {} 已存在，改为按规则自动生成", candidate);
                    }
                }
            }
            if (StringUtils.isEmpty(processNo))
            {
                processNo = processNoService.next(definitionKey, vars);
            }
            if (StringUtils.isNotEmpty(processNo))
            {
                vars.put(FlowSystemFields.FIELD_PROCESS_NO, processNo);
            }
            // 表单字段里已有「流程编号」但业务表还没这一列（字段保存与重建表之间的时间差）时补上：
            // 插入语句会带上该列，缺列会直接报 Unknown column 导致发不出去
            if (form.getFieldsJson() != null
                    && form.getFieldsJson().contains("\"" + FlowSystemFields.FIELD_PROCESS_NO + "\""))
            {
                ensureProcessNoColumn(form.getTableName());
            }
            businessKey = insertFormData(form, vars);
        }
        Long userId = SecurityUtils.getUserId();
        org.flowable.common.engine.impl.identity.Authentication
                .setAuthenticatedUserId(String.valueOf(userId));
        try
        {
            org.flowable.engine.runtime.ProcessInstance instance = runtimeService
                    .startProcessInstanceById(definition.getId(), businessKey, vars);
            // 发起时的签字意见：写一条流程级批注（taskId 为空），供「审批意见」发起行展示；
            // 失败不影响发起（这里已经启动成功，注释写不进去也不能把单子搅黄）
            try
            {
                Object signVar = vars.get(FlowSystemFields.FIELD_SIGN_OPINION);
                String sign = signVar == null ? null : String.valueOf(signVar);
                if (StringUtils.isNotEmpty(sign)
                        && StringUtils.isNotEmpty(sign.replaceAll("<[^>]*>", "").replace("&nbsp;", "").trim()))
                {
                    taskService.addComment(null, instance.getId(), "comment", "【start】" + sign);
                }
            }
            catch (Exception e)
            {
                log.warn("发起签字意见写入失败：{}", e.getMessage());
            }
            // 流程标题：优先取表单里的「标题」系统字段（用户可检索），其次用入参 title
            String instanceTitle = null;
            if (vars != null && vars.get(FlowSystemFields.FIELD_TITLE) != null)
            {
                instanceTitle = String.valueOf(vars.get(FlowSystemFields.FIELD_TITLE));
            }
            if (StringUtils.isEmpty(instanceTitle))
            {
                instanceTitle = title;
            }
            if (StringUtils.isNotEmpty(instanceTitle))
            {
                runtimeService.setProcessInstanceName(instance.getId(), instanceTitle);
            }
            // 回填业务数据行的流程实例ID
            if (formId != null && businessKey != null)
            {
                FlowForm form = flowFormMapper.selectFlowFormByFormId(formId);
                if (form != null && StringUtils.isNotEmpty(form.getTableName()))
                {
                    jdbcTemplate.update("UPDATE `" + form.getTableName()
                            + "` SET process_instance_id = ? WHERE id = ?", instance.getId(), businessKey);
                }
            }
            return instance.getId();
        }
        finally
        {
            org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(null);
        }
    }

    /**
     * 编号是否已被占用（发起人手工改号时判重用）。
     * 表或列不存在、查询异常一律按「不重复」处理：宁可发号成功，也不要因为判重把单子挡住。
     */
    private boolean processNoExists(String tableName, String processNo)
    {
        if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(processNo))
        {
            return false;
        }
        try
        {
            Integer col = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()"
                            + " AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class, tableName, FlowSystemFields.FIELD_PROCESS_NO);
            if (col == null || col == 0)
            {
                return false;
            }
            Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `" + tableName
                    + "` WHERE `" + FlowSystemFields.FIELD_PROCESS_NO + "` = ?", Integer.class, processNo);
            return cnt != null && cnt > 0;
        }
        catch (Exception e)
        {
            log.warn("流程编号判重失败（{}）：{}", processNo, e.getMessage());
            return false;
        }
    }

    /**
     * 老业务表没有「流程编号」列时补上。
     * 表单保存（fieldsJson 已含 processNo）与重建数据表之间有时间差，期间发起会撞 Unknown column，
     * 这里只补这一列，避免为了一次加列去跑整套重建。
     */
    private void ensureProcessNoColumn(String tableName)
    {
        if (StringUtils.isEmpty(tableName))
        {
            return;
        }
        try
        {
            Long cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                    Long.class, tableName, FlowSystemFields.FIELD_PROCESS_NO);
            if (cnt == null || cnt == 0)
            {
                jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD COLUMN `"
                        + FlowSystemFields.FIELD_PROCESS_NO + "` varchar(64) COMMENT '流程编号'");
            }
        }
        catch (Exception e)
        {
            log.warn("补齐流程编号列失败（{}）：{}", tableName, e.getMessage());
        }
    }

    /** 表单数据写入动态数据表（仅主表字段），返回自增ID */
    private String insertFormData(FlowForm form, Map<String, Object> data)
    {
        List<JSONObject> fields = new ArrayList<>();
        for (JSONObject f : parseFields(form.getFieldsJson()))
        {
            normalizeField(f);
            String table = f.getString("table");
            if (table == null || "main".equals(table))
            {
                fields.add(f);
            }
        }
        StringBuilder cols = new StringBuilder("`process_instance_id`");
        StringBuilder marks = new StringBuilder("?");
        List<Object> params = new ArrayList<>();
        params.add(null); // 实例ID稍后回填占位，此处先插空
        for (JSONObject f : fields)
        {
            String col = f.getString("field");
            cols.append(", `").append(col).append("`");
            marks.append(", ?");
            Object v = data.get(col);
            // 系统字段默认值兜底：紧急程度默认「普通(1)」、打印次数默认 0
            if (v == null || StringUtils.isEmpty(String.valueOf(v)))
            {
                if (FlowSystemFields.FIELD_URGENCY.equals(col))
                {
                    v = "1";
                }
                else if (FlowSystemFields.FIELD_PRINT_COUNT.equals(col))
                {
                    v = 0;
                }
            }
            params.add(v == null ? null : String.valueOf(v));
        }
        cols.append(", `create_by`, `create_time`");
        marks.append(", ?, NOW()");
        params.add(SecurityUtils.getUsername());
        String sql = "INSERT INTO `" + form.getTableName() + "` (" + cols + ") VALUES (" + marks + ")";
        jdbcTemplate.update(sql, params.toArray());
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return String.valueOf(id);
    }

    /** 字段JSON解析与基础校验 */
    private List<JSONObject> parseFields(String fieldsJson)
    {
        if (StringUtils.isEmpty(fieldsJson))
        {
            throw new ServiceException("表单未设计任何字段");
        }
        JSONArray arr = JSON.parseArray(fieldsJson);
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++)
        {
            JSONObject f = arr.getJSONObject(i);
            if (StringUtils.isEmpty(f.getString("field")) || StringUtils.isEmpty(f.getString("label")))
            {
                throw new ServiceException("第" + (i + 1) + "个字段缺少字段名或显示名");
            }
            list.add(f);
        }
        if (list.isEmpty())
        {
            throw new ServiceException("表单未设计任何字段");
        }
        return list;
    }

    private void validateFields(String fieldsJson)
    {
        if (StringUtils.isEmpty(fieldsJson))
        {
            return;
        }
        JSONArray arr = JSON.parseArray(fieldsJson);
        // 新建表单允许暂无字段（先建表单、后设计字段）
        if (arr == null || arr.isEmpty())
        {
            return;
        }
        for (int i = 0; i < arr.size(); i++)
        {
            JSONObject f = arr.getJSONObject(i);
            if (StringUtils.isEmpty(f.getString("field")) || StringUtils.isEmpty(f.getString("label")))
            {
                throw new ServiceException("第" + (i + 1) + "个字段缺少字段名或显示名");
            }
            // 系统内置字段（title/urgency/signOpinion/printCount/procStatus）由平台定义，不参与命名校验
            if (!f.getBooleanValue(FlowSystemFields.SYS_FIELD_FLAG)
                    && !f.getString("field").matches("^[a-z][a-z0-9_]{0,39}$"))
            {
                throw new ServiceException("字段名[" + f.getString("field")
                        + "]不合法：仅允许小写字母开头的字母/数字/下划线");
            }
        }
        // 同一张表内数据库字段名不得重复（主表与明细表为独立物理表，跨表允许同名）
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < arr.size(); i++)
        {
            JSONObject f = arr.getJSONObject(i);
            String tbl = StringUtils.isEmpty(f.getString("table")) ? "main" : f.getString("table");
            if (!seen.add(tbl + "." + f.getString("field")))
            {
                throw new ServiceException("字段名[" + f.getString("field") + "]在"
                        + ("main".equals(tbl) ? "主表" : "明细表 " + tbl) + "中重复出现，请修改后再保存");
            }
        }
    }
}
