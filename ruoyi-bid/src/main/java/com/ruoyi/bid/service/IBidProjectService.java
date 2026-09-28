package com.ruoyi.bid.service;

import java.util.List;
import com.ruoyi.bid.domain.BidProject;
import com.ruoyi.bid.domain.BidProjectNode;

/**
 * 投标项目 Service 接口
 */
public interface IBidProjectService
{
    public List<BidProject> selectProjectList(BidProject project);

    public BidProject selectProjectById(Long projectId);

    public int updateProject(BidProject project);

    /** 节点维护（计划时间/实际完成/卡点说明/完成标记） */
    public int updateNode(BidProjectNode node);

    public int deleteProjectByIds(Long[] projectIds);

    /** 状态机流转 */
    public int changeStatus(Long projectId, String targetStatus, String reason);

    /** 完成节点 */
    public int finishNode(Long nodeId, String blockDesc);

    /** 中标转 PMS 立项（返回提示信息） */
    public String toPms(Long projectId);
}
