package com.ruoyi.bid.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 投标进度节点对象 bid_project_node
 */
public class BidProjectNode extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long nodeId;
    private Long projectId;
    /** 节点类型（字典 bid_node_type） */
    private String nodeType;
    private String nodeName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualTime;
    private Long handlerId;
    private String handlerName;
    private String blockDesc;
    /** 是否完成（0未完成 1已完成） */
    private String finishFlag;
    private Integer sortOrder;
    private String delFlag;

    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public Date getPlanTime() { return planTime; }
    public void setPlanTime(Date planTime) { this.planTime = planTime; }
    public Date getActualTime() { return actualTime; }
    public void setActualTime(Date actualTime) { this.actualTime = actualTime; }
    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }
    public String getHandlerName() { return handlerName; }
    public void setHandlerName(String handlerName) { this.handlerName = handlerName; }
    public String getBlockDesc() { return blockDesc; }
    public void setBlockDesc(String blockDesc) { this.blockDesc = blockDesc; }
    public String getFinishFlag() { return finishFlag; }
    public void setFinishFlag(String finishFlag) { this.finishFlag = finishFlag; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
