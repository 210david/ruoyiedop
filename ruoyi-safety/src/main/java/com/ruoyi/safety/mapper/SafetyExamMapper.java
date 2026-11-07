package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyExam;
import com.ruoyi.safety.domain.SafetyExamRecord;

/**
 * 考试场次 Mapper接口
 *
 * @author ruoyi
 */
public interface SafetyExamMapper
{
    public SafetyExam selectSafetyExamById(Long examId);

    public List<SafetyExam> selectSafetyExamList(SafetyExam query);

    public int insertSafetyExam(SafetyExam exam);

    public int updateSafetyExam(SafetyExam exam);

    public int deleteSafetyExamById(Long examId);

    public int deleteSafetyExamByIds(Long[] examIds);

    /** 批量插入考生记录（发布场次生成考名单） */
    public int batchInsertExamRecords(@Param("list") List<SafetyExamRecord> records);

    /** 统计场次已交卷人数 */
    public int countSubmittedByExamId(Long examId);

    /** 统计场次应考人数 */
    public int countTotalByExamId(Long examId);

    /** 按试卷统计引用的考试场次数（未删除且未撤回） */
    public int countByPaperId(Long paperId);

    /** FR-8：查询场次中不合格或缺考且有系统账号的考生记录（补考名单来源） */
    public List<com.ruoyi.safety.domain.SafetyExamRecord> selectRetakeTargets(Long examId);

    /** 已结束待缺考标记的场次ID（定时任务扫描） */
    public List<Long> selectEndedExamIds();

    /** 惰性状态流转兜底：将已到时间未流转的场次批量纠正（定时任务 + 查询兜底共用） */
    public int updateExamStatusByTime();
}
