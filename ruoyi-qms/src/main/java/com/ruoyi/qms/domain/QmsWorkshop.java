package com.ruoyi.qms.domain;

import java.util.List;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 车间/产线/工位表 qms_workshop
 *
 * 树形结构，parent_id=0为根节点
 * 层级：车间(level 1) → 产线(level 2) → 工位(level 3) → 检验点(level 4)
 *
 * @author ruoyi
 */
public class QmsWorkshop extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 节点ID */
    private Long workshopId;

    /** 父节点ID（0=根节点） */
    private Long parentId;

    /** 节点编码 */
    @Excel(name = "节点编码")
    private String workshopCode;

    /** 节点名称 */
    @Excel(name = "节点名称")
    private String workshopName;

    /** 层级类型（1车间 2产线 3工位 4检验点） */
    @Excel(name = "层级类型", readConverterExp = "1=车间,2=产线,3=工位,4=检验点")
    private String nodeType;

    /** 产线类型（0自动化 1半自动 2手工，仅node_type=2时使用） */
    @Excel(name = "产线类型", readConverterExp = "0=自动化,1=半自动,2=手工")
    private String lineType;

    /** 节拍（秒，仅node_type=2时使用） */
    @Excel(name = "节拍(秒)")
    private Integer cycleTime;

    /** 工位类型（0加工 1检验 2包装 3暂存，仅node_type=3时使用） */
    @Excel(name = "工位类型", readConverterExp = "0=加工,1=检验,2=包装,3=暂存")
    private String stationType;

    /** 排序号 */
    private Integer orderNum;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 子节点列表（非数据库字段） */
    private List<QmsWorkshop> children;

    public Long getWorkshopId() { return workshopId; }
    public void setWorkshopId(Long workshopId) { this.workshopId = workshopId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getWorkshopCode() { return workshopCode; }
    public void setWorkshopCode(String workshopCode) { this.workshopCode = workshopCode; }
    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getLineType() { return lineType; }
    public void setLineType(String lineType) { this.lineType = lineType; }
    public Integer getCycleTime() { return cycleTime; }
    public void setCycleTime(Integer cycleTime) { this.cycleTime = cycleTime; }
    public String getStationType() { return stationType; }
    public void setStationType(String stationType) { this.stationType = stationType; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<QmsWorkshop> getChildren() { return children; }
    public void setChildren(List<QmsWorkshop> children) { this.children = children; }
}
