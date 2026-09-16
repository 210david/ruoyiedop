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

    /** 字段类型 → 数据库类型 */
    private static final Map<String, String> COLUMN_TYPES = Map.of(
            "text", "varchar(500)",
            "textarea", "text",
            "number", "decimal(18,2)",
            "date", "datetime",
            "select", "varchar(255)"
    );

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
        return flowFormMapper.selectFlowFormByProcessKey(processKey);
    }

    @Override
    @Transactional
    public int insertFlowForm(FlowForm form)
    {
        if (flowFormMapper.selectFlowFormByFormKey(form.getFormKey()) != null)
        {
            throw new ServiceException("表单Key已存在：" + form.getFormKey());
        }
        validateFields(form.getFieldsJson());
        SysUser user = userService.selectUserById(SecurityUtils.getUserId());
        form.setCreateBy(user.getUserName());
        return flowFormMapper.insertFlowForm(form);
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
        if (!formKey.matches("^[a-z][a-z0-9_]{1,40}$"))
        {
            throw new ServiceException("表单Key仅允许小写字母开头的字母/数字/下划线（1-40位），当前：" + formKey);
        }
        String tableName = "flow_data_" + formKey;
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");
        ddl.append("  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n");
        for (JSONObject f : parseFields(form.getFieldsJson()))
        {
            String col = f.getString("field");
            if (!col.matches("^[a-z][a-z0-9_]{0,39}$"))
            {
                throw new ServiceException("字段名[" + col + "]不合法：仅允许小写字母开头的字母/数字/下划线");
            }
            String type = COLUMN_TYPES.getOrDefault(f.getString("type"), "varchar(500)");
            ddl.append("  `").append(col).append("` ").append(type).append(" COMMENT '")
               .append(f.getString("label")).append("',\n");
        }
        ddl.append("  `process_instance_id` varchar(64) COMMENT '流程实例ID',\n");
        ddl.append("  `create_by` varchar(64) COMMENT '创建人',\n");
        ddl.append("  `create_time` datetime COMMENT '创建时间',\n");
        ddl.append("  PRIMARY KEY (`id`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='")
           .append(form.getFormName()).append("流程数据'");
        jdbcTemplate.execute(ddl.toString());
        // 回写表名
        FlowForm update = new FlowForm();
        update.setFormId(formId);
        update.setTableName(tableName);
        update.setUpdateBy(SecurityUtils.getUsername());
        flowFormMapper.updateFlowForm(update);
        return tableName;
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

    /** 表单数据写入动态数据表，返回自增ID */
    private String insertFormData(FlowForm form, Map<String, Object> data)
    {
        List<JSONObject> fields = parseFields(form.getFieldsJson());
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
        for (JSONObject f : parseFields(fieldsJson))
        {
            if (!f.getString("field").matches("^[a-z][a-z0-9_]{0,39}$"))
            {
                throw new ServiceException("字段名[" + f.getString("field")
                        + "]不合法：仅允许小写字母开头的字母/数字/下划线");
            }
        }
    }
}
