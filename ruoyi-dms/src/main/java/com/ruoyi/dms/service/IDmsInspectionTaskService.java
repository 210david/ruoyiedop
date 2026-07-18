package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.dms.domain.DmsInspectionTask;

public interface IDmsInspectionTaskService
{
    public List<DmsInspectionTask> selectTaskList(DmsInspectionTask task);
    public DmsInspectionTask selectTaskById(Long taskId);
    public int insertTask(DmsInspectionTask task);
    public int updateTask(DmsInspectionTask task);
    public int deleteTaskByIds(Long[] taskIds);

    /** 开始执行点检任务，记录开始时间 */
    public int startTask(Long taskId);

    /** 完成点检任务，异常项自动转工单 */
    public int completeTask(DmsInspectionTask task);

    /** 根据巡检路线周期自动生成点检任务 */
    public int autoGenerateTasks();

    /** 将过期未完成的点检任务标记为逾期 */
    public int markOverdueTasks();
}
