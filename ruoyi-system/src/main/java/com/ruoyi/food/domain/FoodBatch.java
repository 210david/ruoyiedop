package com.ruoyi.food.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品批次主表对象 food_batch
 *
 * @author ruoyi
 */
public class FoodBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 批次ID */
    private Long batchId;

    /** 内部批次号 */
    @Excel(name = "批次号")
    private String batchCode;

    /** 外部批次号（供应商批次） */
    @Excel(name = "外部批次号")
    private String extBatchCode;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称（关联查询） */
    @Excel(name = "物料名称")
    private String materialName;

    /** 批次来源（1采购入库 2生产完工 3拆批 4合批 5退货 6初始化） */
    @Excel(name = "批次来源", readConverterExp = "1=采购入库,2=生产完工,3=拆批,4=合批,5=退货,6=初始化")
    private String sourceType;

    /** 来源单据类型 */
    private String sourceBillType;

    /** 来源单据ID */
    private Long sourceBillId;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生产日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date produceDate;

    /** 收货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "收货日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date receiveDate;

    /** 保质期天数 */
    private Integer shelfLifeDays;

    /** 到期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "到期日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 批次数量 */
    @Excel(name = "批次数量")
    private BigDecimal qty;

    /** 数量单位 */
    @Excel(name = "单位")
    private String qtyUnit;

    /** 批次状态（0待检 1合格 2冻结 3报废） */
    private String batchStatus;

    /** 效期状态（0正常 1临期 2过期） */
    private String expireStatus;

    /** 冻结原因 */
    private String freezeReason;

    /** 冻结时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date freezeTime;

    /** 删除标志 */
    private String delFlag;

    /** ---- 查询辅助字段（非表字段） ---- */
    private Date beginProduceDate;
    private Date endProduceDate;
    private Date beginExpireDate;
    private Date endExpireDate;

    /** 谱系查询结果（非表字段） */
    private List<FoodBatchGenealogy> genealogyList;

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public String getBatchCode()
    {
        return batchCode;
    }

    public void setBatchCode(String batchCode)
    {
        this.batchCode = batchCode;
    }

    public String getExtBatchCode()
    {
        return extBatchCode;
    }

    public void setExtBatchCode(String extBatchCode)
    {
        this.extBatchCode = extBatchCode;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialCode()
    {
        return materialCode;
    }

    public void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getSourceBillType()
    {
        return sourceBillType;
    }

    public void setSourceBillType(String sourceBillType)
    {
        this.sourceBillType = sourceBillType;
    }

    public Long getSourceBillId()
    {
        return sourceBillId;
    }

    public void setSourceBillId(Long sourceBillId)
    {
        this.sourceBillId = sourceBillId;
    }

    public Date getProduceDate()
    {
        return produceDate;
    }

    public void setProduceDate(Date produceDate)
    {
        this.produceDate = produceDate;
    }

    public Date getReceiveDate()
    {
        return receiveDate;
    }

    public void setReceiveDate(Date receiveDate)
    {
        this.receiveDate = receiveDate;
    }

    public Integer getShelfLifeDays()
    {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(Integer shelfLifeDays)
    {
        this.shelfLifeDays = shelfLifeDays;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    public BigDecimal getQty()
    {
        return qty;
    }

    public void setQty(BigDecimal qty)
    {
        this.qty = qty;
    }

    public String getQtyUnit()
    {
        return qtyUnit;
    }

    public void setQtyUnit(String qtyUnit)
    {
        this.qtyUnit = qtyUnit;
    }

    public String getBatchStatus()
    {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus)
    {
        this.batchStatus = batchStatus;
    }

    public String getExpireStatus()
    {
        return expireStatus;
    }

    public void setExpireStatus(String expireStatus)
    {
        this.expireStatus = expireStatus;
    }

    public String getFreezeReason()
    {
        return freezeReason;
    }

    public void setFreezeReason(String freezeReason)
    {
        this.freezeReason = freezeReason;
    }

    public Date getFreezeTime()
    {
        return freezeTime;
    }

    public void setFreezeTime(Date freezeTime)
    {
        this.freezeTime = freezeTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public Date getBeginProduceDate()
    {
        return beginProduceDate;
    }

    public void setBeginProduceDate(Date beginProduceDate)
    {
        this.beginProduceDate = beginProduceDate;
    }

    public Date getEndProduceDate()
    {
        return endProduceDate;
    }

    public void setEndProduceDate(Date endProduceDate)
    {
        this.endProduceDate = endProduceDate;
    }

    public Date getBeginExpireDate()
    {
        return beginExpireDate;
    }

    public void setBeginExpireDate(Date beginExpireDate)
    {
        this.beginExpireDate = beginExpireDate;
    }

    public Date getEndExpireDate()
    {
        return endExpireDate;
    }

    public void setEndExpireDate(Date endExpireDate)
    {
        this.endExpireDate = endExpireDate;
    }

    public List<FoodBatchGenealogy> getGenealogyList()
    {
        return genealogyList;
    }

    public void setGenealogyList(List<FoodBatchGenealogy> genealogyList)
    {
        this.genealogyList = genealogyList;
    }
}
