package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmProject;

/**
 * 项目立项 Mapper 接口
 */
public interface PjmProjectMapper
{
    public PjmProject selectProjectById(Long projectId);
    public List<PjmProject> selectProjectList(PjmProject project);
    public int insertProject(PjmProject project);
    public int updateProject(PjmProject project);
    public int deleteProjectById(Long projectId);
    public int deleteProjectByIds(Long[] projectIds);

    /** 刷新项目采购汇总（来自 PMS 聚合） */
    public int refreshPurchaseSummary(Long projectId);

    /** 刷新项目进度（来自 WBS 任务平均进度） */
    public int refreshProgress(Long projectId);
}
