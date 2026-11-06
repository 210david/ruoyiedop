package com.ruoyi.pjm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pjm.domain.PjmTaskWorklog;
import com.ruoyi.pjm.mapper.PjmProjectMemberMapper;
import com.ruoyi.pjm.mapper.PjmTaskWorklogMapper;
import com.ruoyi.pjm.service.IPjmTaskWorklogService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class PjmTaskWorklogServiceImpl implements IPjmTaskWorklogService
{
    @Autowired
    private PjmTaskWorklogMapper worklogMapper;
    @Autowired
    private PjmProjectMemberMapper memberMapper;
    @Autowired
    private IMkNumberRuleService numberRuleService;

    @Override
    public PjmTaskWorklog selectWorklogById(Long worklogId) { return worklogMapper.selectWorklogById(worklogId); }

    @Override
    public List<PjmTaskWorklog> selectWorklogList(PjmTaskWorklog worklog) { return worklogMapper.selectWorklogList(worklog); }

    @Override
    @Transactional
    public int insertWorklog(PjmTaskWorklog worklog)
    {
        if (worklog.getWorklogNo() == null)
        {
            worklog.setWorklogNo(numberRuleService.generateNumber("PJM_WORKLOG"));
        }
        if (worklog.getUserId() == null) worklog.setUserId(SecurityUtils.getUserId());
        if (worklog.getUserName() == null) worklog.setUserName(SecurityUtils.getUsername());
        if (worklog.getWorkDate() == null) worklog.setWorkDate(new Date());
        worklog.setDelFlag("0");
        worklog.setCreateTime(new Date());
        worklog.setCreateBy(SecurityUtils.getUsername());
        int rows = worklogMapper.insertWorklog(worklog);
        refreshRelated(worklog.getTaskId(), worklog.getUserId());
        return rows;
    }

    @Override
    @Transactional
    public int updateWorklog(PjmTaskWorklog worklog)
    {
        PjmTaskWorklog existing = worklogMapper.selectWorklogById(worklog.getWorklogId());
        worklog.setUpdateTime(new Date());
        worklog.setUpdateBy(SecurityUtils.getUsername());
        int rows = worklogMapper.updateWorklog(worklog);
        refreshRelated(existing != null ? existing.getTaskId() : null,
                       existing != null ? existing.getUserId() : null);
        return rows;
    }

    @Override
    @Transactional
    public int deleteWorklogByIds(Long[] worklogIds)
    {
        java.util.Set<Long> taskIds = new java.util.HashSet<>();
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (Long id : worklogIds)
        {
            PjmTaskWorklog w = worklogMapper.selectWorklogById(id);
            if (w != null) { taskIds.add(w.getTaskId()); if (w.getUserId() != null) userIds.add(w.getUserId()); }
        }
        int rows = worklogMapper.deleteWorklogByIds(worklogIds);
        taskIds.forEach(t -> refreshRelated(t, null));
        return rows;
    }

    private void refreshRelated(Long taskId, Long userId)
    {
        if (taskId != null)
        {
            worklogMapper.refreshTaskActualHours(taskId);
        }
    }
}
