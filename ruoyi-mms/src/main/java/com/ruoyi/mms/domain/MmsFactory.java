package com.ruoyi.mms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工厂 mms_factory
 */
public class MmsFactory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long factoryId;

    @Excel(name = "工厂编码")
    private String factoryCode;

    @Excel(name = "工厂名称")
    private String factoryName;

    @Excel(name = "地址")
    private String address;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private Long tenantId;

    private String delFlag;

    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public String getFactoryCode() { return factoryCode; }
    public void setFactoryCode(String factoryCode) { this.factoryCode = factoryCode; }
    public String getFactoryName() { return factoryName; }
    public void setFactoryName(String factoryName) { this.factoryName = factoryName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
