package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程节点打印模板 flow_node_print
 *
 * <p>对标泛微：办理页「打印」不再固定打印当前节点的显示模板，
 * 每个节点可以单独指定一个打印模板；未配置时回退到该节点的显示模板。</p>
 *
 * <p>模板内容取自表单库中某张表单，保存时把字段与布局快照下来，
 * 之后表单库改版不影响已配置节点。</p>
 *
 * @author ruoyi
 */
public class FlowNodePrint extends BaseEntity
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

    /** 节点名称 */
    private String nodeName;

    /** 打印模板来源表单ID（flow_form.form_id） */
    private String templateFormId;

    /** 打印模板名称 */
    private String templateName;

    /** 打印模板字段快照 */
    private String fieldsJson;

    /** 打印模板布局快照 */
    private String layoutJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getTemplateFormId() { return templateFormId; }
    public void setTemplateFormId(String templateFormId) { this.templateFormId = templateFormId; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getFieldsJson() { return fieldsJson; }
    public void setFieldsJson(String fieldsJson) { this.fieldsJson = fieldsJson; }
    public String getLayoutJson() { return layoutJson; }
    public void setLayoutJson(String layoutJson) { this.layoutJson = layoutJson; }
}
