package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程附件对象 flow_attach
 *
 * 办理页「附件上传」菜单上传的文件按流程实例归档，在「相关资源」页签汇总展示。
 *
 * @author ruoyi
 */
public class FlowAttach extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 附件ID */
    private Long attachId;

    /** 流程实例ID */
    private String instanceId;

    /** 上传所在任务ID */
    private String taskId;

    /** 上传所在节点名称 */
    private String taskName;

    /** 文件名 */
    private String fileName;

    /** 文件访问地址 */
    private String fileUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传人用户ID */
    private Long uploaderId;

    /** 上传人姓名 */
    private String uploaderName;

    public Long getAttachId() { return attachId; }
    public void setAttachId(Long attachId) { this.attachId = attachId; }
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
}
