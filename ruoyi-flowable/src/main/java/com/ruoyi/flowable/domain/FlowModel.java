package com.ruoyi.flowable.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程模型/定义视图对象（封装 Flowable Model 与 ProcessDefinition）
 *
 * @author ruoyi
 */
public class FlowModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模型ID */
    private String modelId;

    /** 模型名称 */
    private String modelName;

    /** 模型Key */
    private String modelKey;

    /** 流程分类 */
    private String category;

    /** 模型描述 */
    private String description;

    /** 模型版本 */
    private Integer version;

    /** BPMN XML 内容 */
    private String bpmnXml;

    /** 部署ID（已部署时返回） */
    private String deploymentId;

    /** 部署时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deploymentTime;

    /** 是否已部署：0未部署 1已部署 */
    private String deployed;

    /** 流程定义是否挂起 */
    private Boolean suspended;

    /** 关联表单ID（存于模型 metaInfo） */
    private Long formId;

    /** 关联表单名称（展示用） */
    private String formName;

    /** 是否解除表单绑定 */
    private Boolean unbind;

    /** 关联表单的数据库表名（展示用，来自 FlowForm） */
    private String formTableName;

    /** 关联表单字段数（展示用，来自 FlowForm.fieldsJson） */
    private Integer formFieldCount;

    /** 默认部署方式（new 存为新版本 / replace 覆盖当前版本，存于 metaInfo） */
    private String deployMode;

    /** 流程锁定（锁定后禁止编辑设计，存于 metaInfo） */
    private Boolean locked;

    /** 帮助文档链接（存于 metaInfo） */
    private String helpDoc;

    /** 显示顺序（存于 metaInfo） */
    private Integer orderNum;

    /** 版本备注说明（来自扩展表，展示用） */
    private String remark;

    /** 是否当前活动版本（来自扩展表，展示用：1是 0否） */
    private String activeFlag;

    /**
     * 查询开关：是否按流程 key 聚合、每 key 只返回 1 条（活动版本优先）
     * 不传默认为 true（前台流程发起页据此展示）
     */
    private Boolean onlyActive;

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getBpmnXml() { return bpmnXml; }
    public void setBpmnXml(String bpmnXml) { this.bpmnXml = bpmnXml; }
    public String getDeploymentId() { return deploymentId; }
    public void setDeploymentId(String deploymentId) { this.deploymentId = deploymentId; }
    public Date getDeploymentTime() { return deploymentTime; }
    public void setDeploymentTime(Date deploymentTime) { this.deploymentTime = deploymentTime; }
    public String getDeployed() { return deployed; }
    public void setDeployed(String deployed) { this.deployed = deployed; }
    public Boolean getSuspended() { return suspended; }
    public void setSuspended(Boolean suspended) { this.suspended = suspended; }
    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }
    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }
    public Boolean getUnbind() { return unbind; }
    public void setUnbind(Boolean unbind) { this.unbind = unbind; }
    public String getFormTableName() { return formTableName; }
    public void setFormTableName(String formTableName) { this.formTableName = formTableName; }
    public Integer getFormFieldCount() { return formFieldCount; }
    public void setFormFieldCount(Integer formFieldCount) { this.formFieldCount = formFieldCount; }
    public String getDeployMode() { return deployMode; }
    public void setDeployMode(String deployMode) { this.deployMode = deployMode; }
    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }
    public String getHelpDoc() { return helpDoc; }
    public void setHelpDoc(String helpDoc) { this.helpDoc = helpDoc; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getActiveFlag() { return activeFlag; }
    public void setActiveFlag(String activeFlag) { this.activeFlag = activeFlag; }
    public Boolean getOnlyActive() { return onlyActive; }
    public void setOnlyActive(Boolean onlyActive) { this.onlyActive = onlyActive; }
}
