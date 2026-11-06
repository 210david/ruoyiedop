package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmWbsTask;

/**
 * WBS 任务 Mapper
 */
public interface PjmWbsTaskMapper
{
    PjmWbsTask selectTaskById(Long taskId);

    /** 查项目下所有任务（树结构平铺） */
    List<PjmWbsTask> selectTaskList(PjmWbsTask task);

    /** 查项目下根任务（parentTaskId is null） */
    List<PjmWbsTask> selectRootTasks(Long projectId);

    /** 查某任务的直接子任务 */
    List<PjmWbsTask> selectChildTasks(Long parentTaskId);

    int insertTask(PjmWbsTask task);

    int updateTask(PjmWbsTask task);

    int deleteTaskById(Long taskId);

    int deleteTaskByIds(Long[] taskIds);

    /** 递归删除子任务 */
    int deleteChildTasks(Long parentTaskId);

    /** 按项目 ID 硬删全部任务（清旧存新） */
    int deleteByProjectId(Long projectId);
}
