package com.ruoyi.pdm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * BOM下发记录对象 pdm_mbom_release
 *
 * EBOM→MBOM 下发版本对应关系台账（设计方案 7.4 下发转换 / 协调方案衔接点一 D1）。
 * 同一产品重复下发时旧记录置"已替代"（D3口径）。
 *
 * @author ruoyi
 */
public class PdmMbomRelease extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 下发记录ID */
    private Long releaseId;

    /** 设计BOM ID（pdm_design_bom） */
    private Long bomId;

    /** 设计BOM编号（冗余） */
    @Excel(name = "设计BOM编号")
    private String bomNo;

    /** 产品档案ID */
    private Long productId;

    /** 产品编码（=物料编码） */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称（冗余） */
    @Excel(name = "产品名称")
    private String productName;

    /** 下发的EBOM生效版本号 */
    @Excel(name = "EBOM版本")
    private String ebomVersion;

    /** 接收方BOM ID（mms_bom） */
    @Excel(name = "MBOM ID")
    private Long mmsBomId;

    /** 接收方BOM编号（mms_bom.bom_no） */
    @Excel(name = "MBOM编号")
    private String mmsBomNo;

    /** 接收方BOM版本号（=EBOM版本） */
    @Excel(name = "MBOM版本")
    private String mmsVersion;

    /** 下发明细行数 */
    @Excel(name = "明细行数")
    private Integer itemCount;

    /** 下发状态（字典 pdm_release_status：0=已下发,1=已替代） */
    @Excel(name = "下发状态", dictType = "pdm_release_status")
    private String releaseStatus;

    /** 下发人 */
    @Excel(name = "下发人")
    private String releaseBy;

    /** 下发时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    public Long getReleaseId() { return releaseId; }
    public void setReleaseId(Long releaseId) { this.releaseId = releaseId; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public String getBomNo() { return bomNo; }
    public void setBomNo(String bomNo) { this.bomNo = bomNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getEbomVersion() { return ebomVersion; }
    public void setEbomVersion(String ebomVersion) { this.ebomVersion = ebomVersion; }
    public Long getMmsBomId() { return mmsBomId; }
    public void setMmsBomId(Long mmsBomId) { this.mmsBomId = mmsBomId; }
    public String getMmsBomNo() { return mmsBomNo; }
    public void setMmsBomNo(String mmsBomNo) { this.mmsBomNo = mmsBomNo; }
    public String getMmsVersion() { return mmsVersion; }
    public void setMmsVersion(String mmsVersion) { this.mmsVersion = mmsVersion; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public String getReleaseStatus() { return releaseStatus; }
    public void setReleaseStatus(String releaseStatus) { this.releaseStatus = releaseStatus; }
    public String getReleaseBy() { return releaseBy; }
    public void setReleaseBy(String releaseBy) { this.releaseBy = releaseBy; }
    public Date getReleaseTime() { return releaseTime; }
    public void setReleaseTime(Date releaseTime) { this.releaseTime = releaseTime; }
}
