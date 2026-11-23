package com.ruoyi.oa.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 车辆保险台账对象 biz_oa_vehicle_insurance
 */
public class OaVehicleInsurance extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long insuranceId;

    @Excel(name = "保单号")
    private String policyNo;

    private Long vehicleId;

    @NotBlank(message = "车牌号不能为空")
    @Excel(name = "车牌号")
    private String plateNo;

    @Excel(name = "保险公司")
    private String insurer;

    @NotNull(message = "起保日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "起保日期", width = 120, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    @NotNull(message = "到期日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "到期日期", width = 120, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    @Excel(name = "保费(元)")
    private BigDecimal premium;

    @Excel(name = "联系人")
    private String contactName;

    @Excel(name = "联系电话")
    private String contactPhone;

    @Excel(name = "状态", readConverterExp = "0=有效,1=即将到期,2=已过期")
    private String status;

    private String attachment;

    private String delFlag;

    public Long getInsuranceId() { return insuranceId; }
    public void setInsuranceId(Long insuranceId) { this.insuranceId = insuranceId; }
    public String getPolicyNo() { return policyNo; }
    public void setPolicyNo(String policyNo) { this.policyNo = policyNo; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    public String getInsurer() { return insurer; }
    public void setInsurer(String insurer) { this.insurer = insurer; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public BigDecimal getPremium() { return premium; }
    public void setPremium(BigDecimal premium) { this.premium = premium; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
