package com.ruoyi.flowable.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.flowable.domain.FlowForm;
import com.ruoyi.flowable.mapper.FlowFormMapper;
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
    @Autowired
    private FlowFormMapper flowFormMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.flowable.engine.RepositoryService repositoryService;

    @Autowired
    private org.flowable.engine.RuntimeService runtimeService;

    @Autowired
    private ISysUserService userService;

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
        createOrPatchTable(tableName, form.getFormName() + "流程数据", groups.get("main"), false);
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
        // 回写表名
        FlowForm update = new FlowForm();
        update.setFormId(formId);
        update.setTableName(tableName);
        update.setUpdateBy(SecurityUtils.getUsername());
        flowFormMapper.updateFlowForm(update);
        return tableName;
    }

    /** 建表（不存在则建，存在则补齐缺失列）；isDetail=true 时含 mainid 关联字段 */
    private void createOrPatchTable(String tableName, String comment, List<JSONObject> fields, boolean isDetail)
    {
        if (fields == null || fields.isEmpty())
        {
            return;
        }
        java.util.LinkedHashSet<String> existing = new java.util.LinkedHashSet<>();
        boolean tableExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
            Long.class, tableName) > 0;
        if (tableExists)
        {
            existing.addAll(jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ?",
                String.class, tableName));
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
            }
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
            businessKey = insertFormData(form, vars);
        }
        Long userId = SecurityUtils.getUserId();
        org.flowable.common.engine.impl.identity.Authentication
                .setAuthenticatedUserId(String.valueOf(userId));
        try
        {
            org.flowable.engine.runtime.ProcessInstance instance = runtimeService
                    .startProcessInstanceById(definition.getId(), businessKey, vars);
            // 设置流程标题（历史/查询页展示）
            if (StringUtils.isNotEmpty(title))
            {
                runtimeService.setProcessInstanceName(instance.getId(), title);
            }
            // 设置流程标题（历史/查询页展示）
            if (StringUtils.isNotEmpty(title))
            {
                runtimeService.setProcessInstanceName(instance.getId(), title);
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
            cols.append(", `").append(f.getString("field")).append("`");
            marks.append(", ?");
            Object v = data.get(f.getString("field"));
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
            if (!f.getString("field").matches("^[a-z][a-z0-9_]{0,39}$"))
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
