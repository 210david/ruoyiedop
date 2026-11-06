package com.ruoyi.pjm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目立项附件 pjm_project_attachment
 */
public class PjmProjectAttachment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 附件ID */
    private Long attachmentId;

    /** 关联项目ID */
    private Long projectId;

    /** 类型(1招标文件 2投标文件 3项目图纸 4项目保险) */
    @Excel(name = "附件类型", readConverterExp = "1=招标文件,2=投标文件,3=项目图纸,4=项目保险")
    private String attachmentType;

    /** 原始文件名 */
    @Excel(name = "文件名")
    private String fileName;

    /** 上传路径/URL */
    private String fileUrl;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 扩展名 */
    private String fileExt;

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileExt() { return fileExt; }
    public void setFileExt(String fileExt) { this.fileExt = fileExt; }

    private String delFlag;
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
