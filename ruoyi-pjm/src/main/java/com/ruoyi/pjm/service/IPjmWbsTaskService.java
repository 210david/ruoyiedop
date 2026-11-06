package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmWbsTask;

/**
 * WBS 任务 Service 接口
 */
public interface IPjmWbsTaskService
{
    PjmWbsTask selectTaskById(Long taskId);

    /** 查项目下所有任务（平铺） */
    List<PjmWbsTask> selectTaskList(PjmWbsTask task);

    /** 查项目下任务树（组装父子关系） */
    List<PjmWbsTask> selectTaskTree(Long projectId);

    int insertTask(PjmWbsTask task);

    int updateTask(PjmWbsTask task);

    int deleteTaskByIds(Long[] taskIds);
}
