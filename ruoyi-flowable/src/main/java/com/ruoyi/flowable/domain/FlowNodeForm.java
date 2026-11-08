package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程节点表单模板 flow_node_form
 *
 * @author ruoyi
 */
public class FlowNodeForm extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 流程模型ID */
    private String modelId;

    /** 流程模型Key（与部署后流程定义Key一致） */
    private String modelKey;

    /** BPMN节点ID */
    private String nodeId;

    /** 节点类型：start / userTask / end */
    private String nodeType;

    /** 节点名称 */
    private String nodeName;

    /** 节点模板名称 */
    private String formName;

    /** 引用的数据表表单Key */
    private String formKey;

    /** 字段快照JSON */
    private String fieldsJson;

    /** 布局快照JSON */
    private String layoutJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }
    public String getFormKey() { return formKey; }
    public void setFormKey(String formKey) { this.formKey = formKey; }
    public String getFieldsJson() { return fieldsJson; }
    public void setFieldsJson(String fieldsJson) { this.fieldsJson = fieldsJson; }
    public String getLayoutJson() { return layoutJson; }
    public void setLayoutJson(String layoutJson) { this.layoutJson = layoutJson; }
}
