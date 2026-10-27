package com.ruoyi.mk.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 素材对象 mk_material
 *
 * @author ruoyi
 */
public class MkMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 素材ID */
    private Long materialId;

    /** 素材编号（MAT前缀） */
    @Excel(name = "素材编号")
    private String materialNo;

    /** 素材类型（0文案 1图片 2视频 3其他） */
    @Excel(name = "素材类型", dictType = "marketing_material_type")
    private String materialType;

    /** 素材标题 */
    @Excel(name = "素材标题")
    private String title;

    /** 文案内容（文案型素材） */
    private String content;

    /** 文件URL（文件型素材） */
    private String fileUrl;

    /** 文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 标签（逗号分隔） */
    @Excel(name = "标签")
    private String tags;

    /** 关联活动ID（可选） */
    private Long activityId;

    /** 被引用次数 */
    @Excel(name = "引用次数")
    private Integer useCount;

    /** 创建人ID */
    private Long userId;

    /** 素材状态（0正常 1停用） */
    @Excel(name = "素材状态", dictType = "sys_normal_disable")
    private String materialStatus;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 活动名称（关联查询） */
    private String activityName;

    /** 创建人昵称（关联查询） */
    private String userName;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialNo() { return materialNo; }
    public void setMaterialNo(String materialNo) { this.materialNo = materialNo; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Integer getUseCount() { return useCount; }
    public void setUseCount(Integer useCount) { this.useCount = useCount; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMaterialStatus() { return materialStatus; }
    public void setMaterialStatus(String materialStatus) { this.materialStatus = materialStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
