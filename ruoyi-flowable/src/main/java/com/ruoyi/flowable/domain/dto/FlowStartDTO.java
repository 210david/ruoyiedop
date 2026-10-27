package com.ruoyi.flowable.domain.dto;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;

/**
 * 发起流程入参
 *
 * @author ruoyi
 */
public class FlowStartDTO
{
    /** 流程定义Key（模型Key） */
    @NotBlank(message = "流程定义Key不能为空")
    private String processDefinitionKey;

    /** 业务主键（业务单据ID，可选） */
    private String businessKey;

    /** 流程变量 */
    private Map<String, Object> variables;

    public String getProcessDefinitionKey() { return processDefinitionKey; }
    public void setProcessDefinitionKey(String processDefinitionKey) { this.processDefinitionKey = processDefinitionKey; }
    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}
