package com.ruoyi.bid.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.bid.domain.BidProject;
import com.ruoyi.bid.domain.BidProjectNode;
import com.ruoyi.bid.mapper.BidProjectMapper;
import com.ruoyi.bid.service.IBidProjectService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 投标项目 Service 实现（含状态机、节点、预警）
 */
@Service
public class BidProjectServiceImpl implements IBidProjectService
{
    /** 状态机合法流转表：当前状态 → 允许的目标状态集合 */
    private static final java.util.Map<String, String> TRANSITIONS = new java.util.HashMap<>();
    static
    {
        TRANSITIONS.put("0", "1,2,10");
        TRANSITIONS.put("1", "2,10");
        TRANSITIONS.put("2", "3,10");
        TRANSITIONS.put("3", "4,10");
        TRANSITIONS.put("4", "5,10");
        TRANSITIONS.put("5", "6,10");
        TRANSITIONS.put("6", "7,8,9");
        // 7/8/9/10 终态不允许再流转
    }

    @Autowired
    private BidProjectMapper projectMapper;

    @Override
    public List<BidProject> selectProjectList(BidProject project)
    {
        return projectMapper.selectProjectList(project);
    }

    @Override
    public BidProject selectProjectById(Long projectId)
    {
        BidProject project = projectMapper.selectProjectById(projectId);
        if (project != null)
        {
            project.setNodeList(projectMapper.selectNodeByProjectId(projectId));
        }
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProject(BidProject project)
    {
        validate(project);
        int rows = projectMapper.updateProject(project);
        if (project.getNodeList() != null)
        {
            // 节点整体重建（至少保留一条由前端+validate拦截）
            projectMapper.deleteNodeByProjectId(project.getProjectId());
            saveNodes(project);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNode(BidProjectNode node)
    {
        BidProjectNode exist = projectMapper.selectNodeById(node.getNodeId());
        if (exist == null)
        {
            throw new ServiceException("进度节点不存在");
        }
        BidProjectNode update = new BidProjectNode();
        update.setNodeId(node.getNodeId());
        update.setPlanTime(node.getPlanTime());
        update.setActualTime(node.getActualTime());
        update.setBlockDesc(node.getBlockDesc());
        update.setFinishFlag(node.getFinishFlag());
        update.setHandlerId(node.getHandlerId());
        update.setHandlerName(node.getHandlerName());
        return projectMapper.updateNode(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProjectByIds(Long[] projectIds)
    {
        for (Long id : projectIds)
        {
            BidProject project = projectMapper.selectProjectById(id);
            if (project != null && !"0".equals(project.getStatus()))
            {
                throw new ServiceException("仅草稿状态的投标项目可删除：「" + project.getBidNo() + "」当前不可删除");
            }
            projectMapper.deleteNodeByProjectId(id);
        }
        return projectMapper.deleteProjectByIds(projectIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(Long projectId, String targetStatus, String reason)
    {
        BidProject project = projectMapper.selectProjectById(projectId);
        if (project == null)
        {
            throw new ServiceException("投标项目不存在");
        }
        String allowed = TRANSITIONS.getOrDefault(project.getStatus(), "");
        if (!java.util.Arrays.asList(allowed.split(",")).contains(targetStatus))
        {
            throw new ServiceException("当前状态不允许流转到目标状态");
        }
        // 放弃/未中标/废标须填原因
        if (java.util.Arrays.asList("8", "9", "10").contains(targetStatus) && StringUtils.isEmpty(reason))
        {
            throw new ServiceException("该状态流转必须填写原因");
        }
        BidProject update = new BidProject();
        update.setProjectId(projectId);
        update.setStatus(targetStatus);
        update.setUpdateBy(SecurityUtils.getUsername());
        if (StringUtils.isNotEmpty(reason))
        {
            update.setFailReason(reason);
            update.setRemark("状态流转备注：" + reason);
        }
        if ("6".equals(targetStatus))
        {
            // 开标后自动记录结果公示时间基准
            update.setResultTime(new java.util.Date());
        }
        return projectMapper.updateProject(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishNode(Long nodeId, String blockDesc)
    {
        BidProjectNode node = projectMapper.selectNodeById(nodeId);
        if (node == null)
        {
            throw new ServiceException("节点不存在");
        }
        if ("1".equals(node.getFinishFlag()))
        {
            throw new ServiceException("该节点已完成，请勿重复操作");
        }
        BidProjectNode update = new BidProjectNode();
        update.setNodeId(nodeId);
        update.setFinishFlag("1");
        update.setActualTime(new java.util.Date());
        if (StringUtils.isNotEmpty(blockDesc))
        {
            update.setBlockDesc(blockDesc);
        }
        try
        {
            update.setHandlerId(SecurityUtils.getUserId());
            update.setHandlerName(SecurityUtils.getUsername());
        }
        catch (Exception ignore)
        {
        }
        return projectMapper.updateNode(update);
    }

    @Override
    public String toPms(Long projectId)
    {
        BidProject project = projectMapper.selectProjectById(projectId);
        if (project == null)
        {
            throw new ServiceException("投标项目不存在");
        }
        if (!"7".equals(project.getStatus()))
        {
            throw new ServiceException("只有中标状态的项目才能转立项");
        }
        // PMS 项目立项接口联动：本期实现数据预填信息返回，前端跳转 PMS 立项页带参
        return project.getBidNo();
    }

    private void saveNodes(BidProject project)
    {
        if (project.getNodeList() != null)
        {
            if (project.getNodeList().isEmpty())
            {
                throw new ServiceException("进度节点必须至少保留一条");
            }
            int sort = 1;
            for (BidProjectNode node : project.getNodeList())
            {
                node.setProjectId(project.getProjectId());
                if (node.getSortOrder() == null)
                {
                    node.setSortOrder(sort);
                }
                projectMapper.insertNode(node);
                sort++;
            }
        }
    }

    private void validate(BidProject project)
    {
        if (StringUtils.isEmpty(project.getProjectName()))
        {
            throw new ServiceException("投标项目名称不能为空");
        }
        if (project.getSubmitDeadline() != null && project.getApplyDeadline() != null
                && project.getSubmitDeadline().before(project.getApplyDeadline()))
        {
            throw new ServiceException("递交截止时间不能早于报名截止时间");
        }
        if (project.getOpenTime() != null && project.getSubmitDeadline() != null
                && project.getOpenTime().before(project.getSubmitDeadline()))
        {
            throw new ServiceException("开标时间不能早于递交截止时间");
        }
        if ("2".equals(project.getJoinType()) && StringUtils.isEmpty(project.getPartner()))
        {
            throw new ServiceException("联营承接时必须填写联营合作方");
        }
        if (project.getOwnerUserId() == null)
        {
            throw new ServiceException("投标负责人不能为空");
        }
    }
}
