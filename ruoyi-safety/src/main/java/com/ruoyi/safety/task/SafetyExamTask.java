package com.ruoyi.safety.task;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.safety.mapper.SafetyExamMapper;
import com.ruoyi.safety.mapper.SafetyExamRecordMapper;

/**
 * 考试状态兜底定时任务
 *
 * sys_job 调用目标：safetyExamTask.run
 * 建议频率：每 1 分钟（cron: 0 * * * * ?）
 *
 * 职责：
 * 1. 场次状态惰性流转兜底（1已发布→2考试中→3已结束，按窗口时间）；
 * 2. 已结束场次批量标记缺考（窗口结束仍未交卷者）。
 * 两类操作均幂等，可安全重复执行。
 *
 * @author ruoyi
 */
@Component("safetyExamTask")
public class SafetyExamTask
{
    private static final Logger log = LoggerFactory.getLogger(SafetyExamTask.class);

    @Autowired
    private SafetyExamMapper safetyExamMapper;

    @Autowired
    private SafetyExamRecordMapper safetyExamRecordMapper;

    public void run()
    {
        // 1. 状态流转
        int flowRows = safetyExamMapper.updateExamStatusByTime();
        // 2. 已结束场次缺考标记
        List<Long> endedExamIds = safetyExamMapper.selectEndedExamIds();
        int absentRows = 0;
        for (Long examId : endedExamIds)
        {
            absentRows += safetyExamRecordMapper.markAbsentByExamId(examId);
        }
        if (flowRows > 0 || absentRows > 0)
        {
            log.info("[考试兜底任务] 状态流转 {} 场，标记缺考 {} 人", flowRows, absentRows);
        }
    }
}
