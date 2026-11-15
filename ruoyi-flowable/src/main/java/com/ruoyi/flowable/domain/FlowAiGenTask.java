package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI流程生成任务对象 flow_ai_gen_task
 *
 * @author ruoyi
 */
public class FlowAiGenTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务名称 */
    private String taskName;

    /** AI识别的流程名称 */
    private String processName;

    /** AI识别的流程Key */
    private String processKey;

    /** 流程分类 */
    private String category;

    /** 备注说明（用户补充的生成提示要求） */
    private String remark;

    /** 方案文件名 */
    private String fileName;

    /** 方案文件存储路径 */
    private String filePath;

    /** 文件类型 */
    private String fileType;

    /** 方案抽取文本 */
    private String sourceText;

    /** AI原始输出 */
    private String aiRawOutput;

    /** 结构化结果JSON */
    private String resultJson;

    /** 状态（0解析中 1待确认 2已生成 3失败） */
    private String status;

    /** 失败原因 */
    private String errorMsg;

    /** 生成后的流程模型ID */
    private String modelId;

    /** 生成后的表单ID */
    private Long formId;

    /** 生成后的表单Key */
    private String formKey;

    /** 流程节点数 */
    private Integer nodeCount;

    /** 表单字段数 */
    private Integer fieldCount;

    /** 删除标志 */
    private String delFlag;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public String getProcessName()
    {
        return processName;
    }

    public void setProcessName(String processName)
    {
        this.processName = processName;
    }

    public String getProcessKey()
    {
        return processKey;
    }

    public void setProcessKey(String processKey)
    {
        this.processKey = processKey;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getSourceText()
    {
        return sourceText;
    }

    public void setSourceText(String sourceText)
    {
        this.sourceText = sourceText;
    }

    public String getAiRawOutput()
    {
        return aiRawOutput;
    }

    public void setAiRawOutput(String aiRawOutput)
    {
        this.aiRawOutput = aiRawOutput;
    }

    public String getResultJson()
    {
        return resultJson;
    }

    public void setResultJson(String resultJson)
    {
        this.resultJson = resultJson;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getModelId()
    {
        return modelId;
    }

    public void setModelId(String modelId)
    {
        this.modelId = modelId;
    }

    public Long getFormId()
    {
        return formId;
    }

    public void setFormId(Long formId)
    {
        this.formId = formId;
    }

    public String getFormKey()
    {
        return formKey;
    }

    public void setFormKey(String formKey)
    {
        this.formKey = formKey;
    }

    public Integer getNodeCount()
    {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount)
    {
        this.nodeCount = nodeCount;
    }

    public Integer getFieldCount()
    {
        return fieldCount;
    }

    public void setFieldCount(Integer fieldCount)
    {
        this.fieldCount = fieldCount;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
