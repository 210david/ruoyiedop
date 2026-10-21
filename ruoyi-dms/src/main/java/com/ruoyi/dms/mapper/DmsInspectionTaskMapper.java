package com.ruoyi.dms.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.dms.domain.DmsInspectionTask;

public interface DmsInspectionTaskMapper
{
    public List<DmsInspectionTask> selectTaskList(DmsInspectionTask task);
    public DmsInspectionTask selectTaskById(Long taskId);

    /** 查询今天已存在的点检任务（防重复生成） */
    public List<DmsInspectionTask> selectTodayTasksByRouteId(@org.apache.ibatis.annotations.Param("routeId") Long routeId);

    /** 查询所有待执行且已过期的点检任务 */
    public List<DmsInspectionTask> selectOverdueTasks();
    public int insertTask(DmsInspectionTask task);
    public int updateTask(DmsInspectionTask task);
    public int deleteTaskByIds(Long[] taskIds);

    /** 统计全部点检任务数 */
    public int countAllTasks();

    /** 按状态统计点检任务数 */
    public List<Map<String, Object>> countTaskByStatus();
}
