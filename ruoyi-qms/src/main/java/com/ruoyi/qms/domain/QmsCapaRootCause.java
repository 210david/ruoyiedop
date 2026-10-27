package com.ruoyi.qms.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class QmsCapaRootCause extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long rootCauseId;
    private Long capaId;
    private String capaNo;
    private String analysisType;
    private String why1;
    private String why2;
    private String why3;
    private String why4;
    private String why5;
    private String rootCause;
    private String fishboneData;

    public Long getRootCauseId() { return rootCauseId; }
    public void setRootCauseId(Long rootCauseId) { this.rootCauseId = rootCauseId; }
    public Long getCapaId() { return capaId; }
    public void setCapaId(Long capaId) { this.capaId = capaId; }
    public String getCapaNo() { return capaNo; }
    public void setCapaNo(String capaNo) { this.capaNo = capaNo; }
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public String getWhy1() { return why1; }
    public void setWhy1(String why1) { this.why1 = why1; }
    public String getWhy2() { return why2; }
    public void setWhy2(String why2) { this.why2 = why2; }
    public String getWhy3() { return why3; }
    public void setWhy3(String why3) { this.why3 = why3; }
    public String getWhy4() { return why4; }
    public void setWhy4(String why4) { this.why4 = why4; }
    public String getWhy5() { return why5; }
    public void setWhy5(String why5) { this.why5 = why5; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public String getFishboneData() { return fishboneData; }
    public void setFishboneData(String fishboneData) { this.fishboneData = fishboneData; }
}
