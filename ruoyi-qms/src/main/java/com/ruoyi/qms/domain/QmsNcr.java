package com.ruoyi.qms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不合格品报告表 qms_ncr
 *
 * @author ruoyi
 */
public class QmsNcr extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** NCR ID */
    private Long ncrId;

    /** NCR编号 */
    @Excel(name = "NCR编号")
    private String ncrNo;

    /** 来源类型（inspection/complaint/patrol） */
    @Excel(name = "来源类型")
    private String sourceType;

    /** 来源ID */
    private Long sourceId;

    /** 来源单号 */
    @Excel(name = "来源单号")
    private String sourceNo;

    /** 检验任务ID */
    private Long taskId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    @Excel(name = "供应商")
    private String supplierName;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 不合格数量 */
    @Excel(name = "不合格数量")
    private BigDecimal defectQty;

    /** 缺陷等级（1致命 2严重 3一般 4轻微） */
    @Excel(name = "缺陷等级", readConverterExp = "1=致命,2=严重,3=一般,4=轻微")
    private String defectLevel;

    /** 缺陷描述 */
    @Excel(name = "缺陷描述")
    private String defectDesc;

    /** 处置方式（1退货 2挑选 3返工 4报废 5让步接收） */
    @Excel(name = "处置方式", readConverterExp = "1=退货,2=挑选,3=返工,4=报废,5=让步接收")
    private String disposition;

    /** 是否隔离（0否 1是） */
    @Excel(name = "隔离", readConverterExp = "0=否,1=是")
    private String isolateFlag;

    /** 隔离区域 */
    @Excel(name = "隔离区域")
    private String isolateArea;

    /** NCR状态（0已登记 1评审中 2处置中 3待验证 4已关闭 5已作废） */
    @Excel(name = "NCR状态", readConverterExp = "0=已登记,1=评审中,2=处置中,3=待验证,4=已关闭,5=已作废")
    private String ncrStatus;

    /** 当前审批节点（0=未提交，1/2=审批中，3=处置中，4=待验证） */
    private Integer currentNode;

    /** 最大审批节点数（致命/严重=2，一般/轻微=1） */
    private Integer maxNode;

    /** 发现人ID */
    private Long discovererId;

    /** 发现人名称 */
    @Excel(name = "发现人")
    private String discovererName;

    /** 发现时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发现时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date discoverTime;

    /** 责任部门ID */
    private Long respDeptId;

    /** 责任部门名称 */
    @Excel(name = "责任部门")
    private String respDeptName;

    /** 验证结果（1通过 2不通过） */
    @Excel(name = "验证结果", readConverterExp = "1=通过,2=不通过")
    private String verifyResult;

    /** 验证说明 */
    private String verifyRemark;

    /** 验证人ID */
    private Long verifierId;

    /** 验证人名称 */
    @Excel(name = "验证人")
    private String verifierName;

    /** 验证时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date verifyTime;

    /** 关闭时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;

    /** 状态（0正常 1停用） */
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 审批记录列表（非数据库字段） */
    private List<QmsNcrApproveLog> approveLogList;

    public Long getNcrId() { return ncrId; }
    public void setNcrId(Long ncrId) { this.ncrId = ncrId; }
    public String getNcrNo() { return ncrNo; }
    public void setNcrNo(String ncrNo) { this.ncrNo = ncrNo; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getDefectQty() { return defectQty; }
    public void setDefectQty(BigDecimal defectQty) { this.defectQty = defectQty; }
    public String getDefectLevel() { return defectLevel; }
    public void setDefectLevel(String defectLevel) { this.defectLevel = defectLevel; }
    public String getDefectDesc() { return defectDesc; }
    public void setDefectDesc(String defectDesc) { this.defectDesc = defectDesc; }
    public String getDisposition() { return disposition; }
    public void setDisposition(String disposition) { this.disposition = disposition; }
    public String getIsolateFlag() { return isolateFlag; }
    public void setIsolateFlag(String isolateFlag) { this.isolateFlag = isolateFlag; }
    public String getIsolateArea() { return isolateArea; }
    public void setIsolateArea(String isolateArea) { this.isolateArea = isolateArea; }
    public String getNcrStatus() { return ncrStatus; }
    public void setNcrStatus(String ncrStatus) { this.ncrStatus = ncrStatus; }
    public Long getDiscovererId() { return discovererId; }
    public void setDiscovererId(Long discovererId) { this.discovererId = discovererId; }
    public String getDiscovererName() { return discovererName; }
    public void setDiscovererName(String discovererName) { this.discovererName = discovererName; }
    public Date getDiscoverTime() { return discoverTime; }
    public void setDiscoverTime(Date discoverTime) { this.discoverTime = discoverTime; }
    public Long getRespDeptId() { return respDeptId; }
    public void setRespDeptId(Long respDeptId) { this.respDeptId = respDeptId; }
    public String getRespDeptName() { return respDeptName; }
    public void setRespDeptName(String respDeptName) { this.respDeptName = respDeptName; }
    public String getVerifyResult() { return verifyResult; }
    public void setVerifyResult(String verifyResult) { this.verifyResult = verifyResult; }
    public String getVerifyRemark() { return verifyRemark; }
    public void setVerifyRemark(String verifyRemark) { this.verifyRemark = verifyRemark; }
    public Long getVerifierId() { return verifierId; }
    public void setVerifierId(Long verifierId) { this.verifierId = verifierId; }
    public String getVerifierName() { return verifierName; }
    public void setVerifierName(String verifierName) { this.verifierName = verifierName; }
    public Date getVerifyTime() { return verifyTime; }
    public void setVerifyTime(Date verifyTime) { this.verifyTime = verifyTime; }
    public Date getCloseTime() { return closeTime; }
    public void setCloseTime(Date closeTime) { this.closeTime = closeTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Integer getCurrentNode() { return currentNode; }
    public void setCurrentNode(Integer currentNode) { this.currentNode = currentNode; }
    public Integer getMaxNode() { return maxNode; }
    public void setMaxNode(Integer maxNode) { this.maxNode = maxNode; }
    public List<QmsNcrApproveLog> getApproveLogList() { return approveLogList; }
    public void setApproveLogList(List<QmsNcrApproveLog> approveLogList) { this.approveLogList = approveLogList; }
}
