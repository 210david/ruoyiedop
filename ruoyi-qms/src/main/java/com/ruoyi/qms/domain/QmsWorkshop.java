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
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<QmsWorkshop> getChildren() { return children; }
    public void setChildren(List<QmsWorkshop> children) { this.children = children; }
}
