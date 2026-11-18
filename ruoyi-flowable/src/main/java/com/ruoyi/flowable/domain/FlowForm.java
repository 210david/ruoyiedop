package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程表单定义 flow_form
 *
 * @author ruoyi
 */
public class FlowForm extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 表单ID */
    private Long formId;

    /** 表单Key */
    private String formKey;

    /** 表单名称 */
    private String formName;

    /** 表单描述 */
    private String formDesc;

    /** 流程分类（字典 flowable_category） */
    private String category;

    /** 绑定的流程Key */
    private String processKey;

    /** 字段定义JSON [{field,label,type,required,order}] */
    private String fieldsJson;

    /** 页面布局JSON {sections:[{title,rows:[[ {field,span} ]]}]} */
    private String layoutJson;

    /** 表单扩展JSON {v,rules:[],scripts:[]} */
    private String extJson;

    /** 生成的数据表名 */
    private String tableName;

    /** 删除标志 */
    private String delFlag;

    /** 是否系统流程表单（考勤/车辆/印章等）：不允许删除表单与已有字段，只能新增或修改字段 */
    private Boolean systemFlow;

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }
    public String getFormKey() { return formKey; }
    public void setFormKey(String formKey) { this.formKey = formKey; }
    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }
    public String getFormDesc() { return formDesc; }
    public void setFormDesc(String formDesc) { this.formDesc = formDesc; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }
    public String getFieldsJson() { return fieldsJson; }
    public void setFieldsJson(String fieldsJson) { this.fieldsJson = fieldsJson; }
    public String getLayoutJson() { return layoutJson; }
    public void setLayoutJson(String layoutJson) { this.layoutJson = layoutJson; }
    public String getExtJson() { return extJson; }
    public void setExtJson(String extJson) { this.extJson = extJson; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Boolean getSystemFlow() { return systemFlow; }
    public void setSystemFlow(Boolean systemFlow) { this.systemFlow = systemFlow; }
}
