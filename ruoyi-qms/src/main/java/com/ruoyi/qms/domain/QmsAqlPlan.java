package com.ruoyi.qms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsAqlPlan extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long planId;
    @Excel(name = "AQL等级") private String aqlLevel;
    @Excel(name = "字码") private String codeLetter;
    @Excel(name = "样本量") private Integer sampleSize;
    @Excel(name = "接收数Ac") private Integer acVal;
    @Excel(name = "拒收数Re") private Integer reVal;
    @Excel(name = "检验严格度") private String inspectLevel;
    private Integer batchMin;
    private Integer batchMax;
    private String status;
    private String delFlag;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getAqlLevel() { return aqlLevel; }
    public void setAqlLevel(String aqlLevel) { this.aqlLevel = aqlLevel; }
    public String getCodeLetter() { return codeLetter; }
    public void setCodeLetter(String codeLetter) { this.codeLetter = codeLetter; }
    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }
    public Integer getAcVal() { return acVal; }
    public void setAcVal(Integer acVal) { this.acVal = acVal; }
    public Integer getReVal() { return reVal; }
    public void setReVal(Integer reVal) { this.reVal = reVal; }
    public String getInspectLevel() { return inspectLevel; }
    public void setInspectLevel(String inspectLevel) { this.inspectLevel = inspectLevel; }
    public Integer getBatchMin() { return batchMin; }
    public void setBatchMin(Integer batchMin) { this.batchMin = batchMin; }
    public Integer getBatchMax() { return batchMax; }
    public void setBatchMax(Integer batchMax) { this.batchMax = batchMax; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
