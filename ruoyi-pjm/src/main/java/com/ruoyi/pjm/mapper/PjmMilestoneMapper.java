package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmMilestone;

/**
 * 项目里程碑 Mapper
 */
public interface PjmMilestoneMapper
{
    PjmMilestone selectMilestoneById(Long milestoneId);

    List<PjmMilestone> selectMilestoneList(PjmMilestone milestone);

    int insertMilestone(PjmMilestone milestone);

    int updateMilestone(PjmMilestone milestone);

    int deleteMilestoneByIds(Long[] milestoneIds);
}
