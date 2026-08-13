package com.ruoyi.qms.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.qms.domain.QmsInspTask;

/**
 * 检验任务 Service接口
 *
 * @author ruoyi
 */
public interface IQmsInspTaskService
{
    public List<QmsInspTask> selectInspTaskList(QmsInspTask inspTask);
    public QmsInspTask selectInspTaskById(Long taskId);
    public int insertInspTask(QmsInspTask inspTask);
    public int updateInspTask(QmsInspTask inspTask);
    public int deleteInspTaskByIds(Long[] taskIds);
    public int saveInspectResult(QmsInspTask inspTask);
    public QmsInspTask createRecheckTask(Long taskId);

    /** 定时任务：扫描WMS到货入库单自动生成IQC检验任务 */
    public int autoGenerateIqcTasks();

    /** 定时任务：标记逾期未完成检验任务 */
    public int checkOverdueTasks();

    /** 根据来源单号查询是否已存在检验任务 */
    public int countBySourceNo(String sourceNo);

    /** 获取检验报告数据（含明细、电子签名） */
    public QmsInspTask getReportData(Long taskId);

    /** 开始检验：待检(0) → 检验中(1) */
    public int startInspect(Long taskId);

    /** 作废检验任务 */
    public int voidTask(Long taskId, String reason, String voidType);

    /** 批量分配检验员 */
    public int assignInspector(Long[] taskIds, Long inspectorId, String inspectorName);

    /** 查询各状态任务数量（状态标签栏计数） */
    public Map<String, Integer> selectStatusCounts();
}
