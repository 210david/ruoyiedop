package com.ruoyi.flowable.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程定义扩展对象（版本备注 / 活动版本标志）
 *
 * 配合流程版本管理：用户可选择启用哪个版本（设为活动版本）、每个版本可填写备注说明。
 * Flowable 原生表不存备注/活动版本，此表按 procdef_id 一对一扩展。
 *
 * @author ruoyi
 */
public class FlowProcdefExt implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 流程定义ID（ACT_RE_PROCDEF.ID_） */
    private String procdefId;

    /** 流程Key（ACT_RE_PROCDEF.KEY_） */
    private String procKey;

    /** 版本号（ACT_RE_PROCDEF.VERSION_） */
    private Integer version;

    /** 版本备注说明 */
    private String remark;

    /** 是否活动版本（1当前活动版本 0非活动） */
    private String activeFlag;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public String getProcdefId() { return procdefId; }
    public void setProcdefId(String procdefId) { this.procdefId = procdefId; }
    public String getProcKey() { return procKey; }
    public void setProcKey(String procKey) { this.procKey = procKey; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getActiveFlag() { return activeFlag; }
    public void setActiveFlag(String activeFlag) { this.activeFlag = activeFlag; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
