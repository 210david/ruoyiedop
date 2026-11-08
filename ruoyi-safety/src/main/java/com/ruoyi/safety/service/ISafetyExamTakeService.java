package com.ruoyi.safety.service;

import java.util.List;
import java.util.Map;

/**
 * 考生端考试 Service接口
 *
 * @author ruoyi
 */
public interface ISafetyExamTakeService
{
    /** 我的考试列表（聚合场次窗口与试卷规则） */
    public List<Map<String, Object>> selectMyExamList(Long userId);

    /** 进入考试：校验窗口与归属，返回脱敏试卷快照 + 已有草稿 + 剩余秒数 */
    public Map<String, Object> startExam(Long recordId, Long userId);

    /** 保存答题草稿（进行中可反复调用） */
    public int saveDraft(Long recordId, List<Map<String, Object>> answers, Long userId);

    /** 交卷并自动判分（客观题；判分后回写考生成绩） */
    public Map<String, Object> submitExam(Long recordId, List<Map<String, Object>> answers, Integer switchCount, String submitType, Long userId);

    /** 查看成绩与解析（仅已判分） */
    public Map<String, Object> getResult(Long recordId, Long userId);
}
