package com.ruoyi.bid.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bid.domain.BidProject;
import com.ruoyi.bid.domain.BidProjectNode;

/**
 * 投标项目 Mapper 接口
 */
public interface BidProjectMapper
{
    public List<BidProject> selectProjectList(BidProject project);

    public BidProject selectProjectById(Long projectId);

    public int insertProject(BidProject project);

    public int updateProject(BidProject project);

    public int deleteProjectByIds(Long[] projectIds);

    public List<BidProjectNode> selectNodeByProjectId(Long projectId);

    public BidProjectNode selectNodeById(Long nodeId);

    public int insertNode(BidProjectNode node);

    public int updateNode(BidProjectNode node);

    public int deleteNodeByProjectId(Long projectId);

    public int deleteNodeByIds(Long[] nodeIds);

    /** 进行中且临近截止的项目（预警扫描） */
    public List<BidProject> selectDeadlineWarnProjects(@Param("days") int days);

    /** 看板统计 */
    public List<Map<String, Object>> selectStatusCount();

    public List<Map<String, Object>> selectMonthlyTrend();

    public List<Map<String, Object>> selectFailReasonCount();

    public Map<String, Object> selectOverview();
}
