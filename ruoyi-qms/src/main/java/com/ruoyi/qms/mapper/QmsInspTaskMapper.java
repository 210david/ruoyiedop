package com.ruoyi.qms.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.qms.domain.QmsInspTask;

/**
 * 检验任务 Mapper
 *
 * @author ruoyi
 */
public interface QmsInspTaskMapper
{
    public List<QmsInspTask> selectInspTaskList(QmsInspTask inspTask);
    public QmsInspTask selectInspTaskById(Long taskId);
    public QmsInspTask selectInspTaskByNo(String taskNo);
    public int insertInspTask(QmsInspTask inspTask);
    public int updateInspTask(QmsInspTask inspTask);
    public int deleteInspTaskByIds(Long[] taskIds);

    /** 根据来源单号统计检验任务数（防重复生成） */
    public int countBySourceNo(String sourceNo);

    /** 查询待检状态且超过指定时间的任务（逾期标记用） */
    public List<QmsInspTask> selectOverdueTasks(int overdueDays);

    /** 查询各状态任务数量（状态标签栏计数） */
    public List<Map<String, Object>> selectStatusCounts();
}
