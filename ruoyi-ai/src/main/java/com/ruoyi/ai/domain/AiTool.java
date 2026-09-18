package com.ruoyi.ai.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI智能查询工具注册 ai_tool
 * 方案3（Text-to-API）：高频问题注册为工具，LLM只做"选工具+填参数"，SQL模板参数化执行
 *
 * @author ruoyi
 */
public class AiTool extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 工具ID */
    private Long toolId;

    /** 工具标识（LLM选择用） */
    @Excel(name = "工具标识")
    private String toolName;

    /** 给LLM看的工具说明 */
    @Excel(name = "工具说明")
    private String description;

    /** 参数JSON Schema */
    private String paramSchema;

    /** 执行类型：sql=模板SQL */
    private String toolType;

    /** SQL模板，:paramName 占位 */
    private String sqlTemplate;

    /** 所需权限标识，空=登录即可 */
    private String permKey;

    /** 状态（0正常 1停用） */
    private String status;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getParamSchema() { return paramSchema; }
    public void setParamSchema(String paramSchema) { this.paramSchema = paramSchema; }
    public String getToolType() { return toolType; }
    public void setToolType(String toolType) { this.toolType = toolType; }
    public String getSqlTemplate() { return sqlTemplate; }
    public void setSqlTemplate(String sqlTemplate) { this.sqlTemplate = sqlTemplate; }
    public String getPermKey() { return permKey; }
    public void setPermKey(String permKey) { this.permKey = permKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
