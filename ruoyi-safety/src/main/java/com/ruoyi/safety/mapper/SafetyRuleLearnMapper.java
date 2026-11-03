package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyRuleLearnTask;
import com.ruoyi.safety.domain.SafetyRuleLearn;

/**
 * 安全守则学习任务/签阅记录 Mapper接口（需求一）
 */
public interface SafetyRuleLearnMapper {
    public List<SafetyRuleLearnTask> selectTaskList(SafetyRuleLearnTask task);

    public SafetyRuleLearnTask selectTaskById(Long taskId);

    public int insertTask(SafetyRuleLearnTask task);

    public int updateTask(SafetyRuleLearnTask task);

    public List<SafetyRuleLearn> selectLearnList(SafetyRuleLearn learn);

    public SafetyRuleLearn selectLearnById(Long learnId);

    /** SR-03：名单只可追加，逐条插入签阅名单 */
    public int insertLearn(SafetyRuleLearn learn);

    /** SR-03：名单只可追加，不允许删除既有名单记录（删除任务时不删签阅名单） */
    public int appendLearnBatch(@Param("learns") List<SafetyRuleLearn> learns);

    /** 签阅确认 */
    public int signLearn(SafetyRuleLearn learn);

    /** 逾期检查：任务与名单同步置逾期 */
    public int markLearnOverdue();

    /** 任务完成度：未签阅人数为 0 时任务自动完成 */
    public int completeTasksIfAllSigned();

    /** 学习任务统计（按任务）：总数/已签阅 */
    public List<SafetyRuleLearnTask> selectTaskStatList();
}
