package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyExamAnswer;
import com.ruoyi.safety.domain.vo.SafetyExamAnswerArchive;
import com.ruoyi.safety.domain.vo.SafetyExamQuestionStat;

/**
 * 考试答题明细 Mapper接口
 *
 * @author ruoyi
 */
public interface SafetyExamAnswerMapper
{
    public SafetyExamAnswer selectSafetyExamAnswerById(Long answerId);

    public List<SafetyExamAnswer> selectSafetyExamAnswerList(SafetyExamAnswer query);

    /** 按考试记录ID查询全部答题明细 */
    public List<SafetyExamAnswer> selectByRecordId(Long recordId);

    public int insertSafetyExamAnswer(SafetyExamAnswer answer);

    /** 草稿实时保存：按 (record_id, paper_question_id) 幂等 upsert */
    public int upsertSafetyExamAnswer(SafetyExamAnswer answer);

    /** 批量 upsert（提交判分用） */
    public int batchUpsert(@Param("list") List<SafetyExamAnswer> list);

    public int updateSafetyExamAnswer(SafetyExamAnswer answer);

    public int deleteSafetyExamAnswerById(Long answerId);

    /** 判分结果回填（单题，Java层循环调用） */
    public int updateJudgeResult(SafetyExamAnswer answer);

    /** FR-7：按场次聚合每题错误率 */
    public List<SafetyExamQuestionStat> selectQuestionStats(Long examId);

    /** FR-12：完整答卷存档明细（每题考生原始作答） */
    public List<SafetyExamAnswerArchive> selectArchiveByExamId(Long examId);
}
