package com.ruoyi.pjm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pjm.domain.PjmMilestone;
import com.ruoyi.pjm.mapper.PjmMilestoneMapper;
import com.ruoyi.pjm.service.IPjmMilestoneService;

/**
 * 项目里程碑 Service 实现
 */
@Service
public class PjmMilestoneServiceImpl implements IPjmMilestoneService
{
    @Autowired
    private PjmMilestoneMapper milestoneMapper;

    @Override
    public PjmMilestone selectMilestoneById(Long milestoneId)
    {
        return milestoneMapper.selectMilestoneById(milestoneId);
    }

    @Override
    public List<PjmMilestone> selectMilestoneList(PjmMilestone milestone)
    {
        return milestoneMapper.selectMilestoneList(milestone);
    }

    @Override
    @Transactional
    public int insertMilestone(PjmMilestone milestone)
    {
        if (milestone.getStatus() == null)
        {
            milestone.setStatus("0");
        }
        milestone.setDelFlag("0");
        milestone.setCreateTime(new Date());
        milestone.setCreateBy(SecurityUtils.getUsername());
        return milestoneMapper.insertMilestone(milestone);
    }

    @Override
    @Transactional
    public int updateMilestone(PjmMilestone milestone)
    {
        milestone.setUpdateTime(new Date());
        milestone.setUpdateBy(SecurityUtils.getUsername());
        // 达成时自动填实际日期
        if ("1".equals(milestone.getStatus()) && milestone.getActualDate() == null)
        {
            milestone.setActualDate(new Date());
        }
        return milestoneMapper.updateMilestone(milestone);
    }

    @Override
    @Transactional
    public int deleteMilestoneByIds(Long[] milestoneIds)
    {
        return milestoneMapper.deleteMilestoneByIds(milestoneIds);
    }
}
