package com.ruoyi.pdm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 技术变更涉及资料明细对象 pdm_change_order_item
 *
 * 勾选自产品当前生效版本清单；执行阶段按变更单对资料升版生效（回填 new_version）。
 *
 * @author ruoyi
 */
public class PdmChangeOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 变更单ID */
    private Long changeId;

    /** 资料类型（字典 pdm_doc_type：1=设计方案,2=图纸文件,3=设计BOM） */
    @Excel(name = "资料类型", dictType = "pdm_doc_type")
    private String docType;

    /** 资料ID */
    private Long bizId;

    /** 资料编号（冗余） */
    @Excel(name = "资料编号")
    private String bizNo;

    /** 资料名称（冗余） */
    @Excel(name = "资料名称")
    private String bizName;

    /** 发起变更时生效版本号 */
    @Excel(name = "当前生效版本")
    private String currentVersion;

    /** 升版后生效版本号（执行时回填） */
    @Excel(name = "升版后版本")
    private String newVersion;

    /** 明细状态（字典 pdm_change_item_status：0=待执行,1=已升版生效） */
    @Excel(name = "明细状态", dictType = "pdm_change_item_status")
    private String itemStatus;

    /** 升版生效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectTime;

    /** 生效操作人 */
    private String effectBy;

    /** 显示顺序 */
    private Integer orderNum;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getChangeId() { return changeId; }
    public void setChangeId(Long changeId) { this.changeId = changeId; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }
    public String getBizName() { return bizName; }
    public void setBizName(String bizName) { this.bizName = bizName; }
    public String getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }
    public String getNewVersion() { return newVersion; }
    public void setNewVersion(String newVersion) { this.newVersion = newVersion; }
    public String getItemStatus() { return itemStatus; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }
    public Date getEffectTime() { return effectTime; }
    public void setEffectTime(Date effectTime) { this.effectTime = effectTime; }
    public String getEffectBy() { return effectBy; }
    public void setEffectBy(String effectBy) { this.effectBy = effectBy; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
}
