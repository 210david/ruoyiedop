package com.ruoyi.pjm.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pjm.domain.PjmWbsTask;
import com.ruoyi.pjm.mapper.PjmProjectMapper;
import com.ruoyi.pjm.mapper.PjmWbsTaskMapper;
import com.ruoyi.pjm.service.IPjmWbsTaskService;

/**
 * WBS 任务 Service 实现
 */
@Service
public class PjmWbsTaskServiceImpl implements IPjmWbsTaskService
{
    @Autowired
    private PjmWbsTaskMapper taskMapper;

    @Autowired
    private PjmProjectMapper projectMapper;

    @Override
    public PjmWbsTask selectTaskById(Long taskId)
    {
        return taskMapper.selectTaskById(taskId);
    }

    @Override
    public List<PjmWbsTask> selectTaskList(PjmWbsTask task)
    {
        return taskMapper.selectTaskList(task);
    }

    @Override
    public List<PjmWbsTask> selectTaskTree(Long projectId)
    {
        PjmWbsTask query = new PjmWbsTask();
        query.setProjectId(projectId);
        List<PjmWbsTask> all = taskMapper.selectTaskList(query);
        return buildTree(all, null);
    }

    private List<PjmWbsTask> buildTree(List<PjmWbsTask> all, Long parentId)
    {
        return all.stream()
                .filter(t -> parentId == null ? t.getParentTaskId() == null : parentId.equals(t.getParentTaskId()))
                .peek(t -> t.setChildren(buildTree(all, t.getTaskId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int insertTask(PjmWbsTask task)
    {
        task.setStatus("0");
        task.setDelFlag("0");
        task.setCreateTime(new Date());
        task.setCreateBy(SecurityUtils.getUsername());
        int rows = taskMapper.insertTask(task);
        refreshProgress(task.getProjectId());
        return rows;
    }

    @Override
    @Transactional
    public int updateTask(PjmWbsTask task)
    {
        task.setUpdateTime(new Date());
        task.setUpdateBy(SecurityUtils.getUsername());
        int rows = taskMapper.updateTask(task);
        refreshProgress(task.getProjectId());
        return rows;
    }

    @Override
    @Transactional
    public int deleteTaskByIds(Long[] taskIds)
    {
        // 先查出项目ID用于刷新进度，再递归删子任务
        List<Long> projectIds = new ArrayList<>();
        for (Long taskId : taskIds)
        {
            PjmWbsTask t = taskMapper.selectTaskById(taskId);
            if (t != null && t.getProjectId() != null)
            {
                projectIds.add(t.getProjectId());
            }
            taskMapper.deleteChildTasks(taskId);
        }
        int rows = taskMapper.deleteTaskByIds(taskIds);
        projectIds.forEach(this::refreshProgress);
        return rows;
    }

    /** 任务变更后刷新项目进度 */
    private void refreshProgress(Long projectId)
    {
        if (projectId != null)
        {
            projectMapper.refreshProgress(projectId);
        }
    }
}
