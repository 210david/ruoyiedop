package com.ruoyi.oa.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 印章台账对象 biz_oa_seal
 */
public class OaSeal extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long sealId;

    @NotBlank(message = "印章名称不能为空")
    @Excel(name = "印章名称")
    private String sealName;

    @Excel(name = "印章编号")
    private String sealCode;

    @Excel(name = "印章类型", dictType = "oa_seal_type")
    private String sealType;

    @Excel(name = "保管人")
    private String keeperName;

    @Excel(name = "保管部门")
    private String keeperDept;

    @Excel(name = "联系电话")
    private String keeperPhone;

    @Excel(name = "存放地点")
    private String keepPlace;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "启用日期", width = 120, dateFormat = "yyyy-MM-dd")
    private Date enableDate;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用,2=已注销")
    private String status;

    /** 关联流程实例ID（预留，后续与印章流程关联） */
    private String flowInstanceId;

    private String attachment;

    private String delFlag;

    public Long getSealId() { return sealId; }
    public void setSealId(Long sealId) { this.sealId = sealId; }
    public String getSealName() { return sealName; }
    public void setSealName(String sealName) { this.sealName = sealName; }
    public String getSealCode() { return sealCode; }
    public void setSealCode(String sealCode) { this.sealCode = sealCode; }
    public String getSealType() { return sealType; }
    public void setSealType(String sealType) { this.sealType = sealType; }
    public String getKeeperName() { return keeperName; }
    public void setKeeperName(String keeperName) { this.keeperName = keeperName; }
    public String getKeeperDept() { return keeperDept; }
    public void setKeeperDept(String keeperDept) { this.keeperDept = keeperDept; }
    public String getKeeperPhone() { return keeperPhone; }
    public void setKeeperPhone(String keeperPhone) { this.keeperPhone = keeperPhone; }
    public String getKeepPlace() { return keepPlace; }
    public void setKeepPlace(String keepPlace) { this.keepPlace = keepPlace; }
    public Date getEnableDate() { return enableDate; }
    public void setEnableDate(Date enableDate) { this.enableDate = enableDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(String flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
